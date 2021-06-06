package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.base.helpers.onFailure
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSObjectRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.EncryptionUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import java.util.logging.Logger

internal class ManipulateIPFSObjectUseCase(
    private val ipfsObjectRepo: IPFSObjectRepository,
    private val ipfsUseCase: IPFSUseCase,
    private val localStorageUseCase: LocalStorageUseCase,
    private val createIPFSObjectUseCase: CreateIPFSObjectUseCase,
    private val encryptionUseCase: EncryptionUseCase,
    private val syncIPFSObjectUseCase: SyncIPFSObjectUseCase,
    private val updateChildObjectsUseCase: UpdateChildObjectsUseCase,
) {
    companion object {
        private val LOGGER = Logger.getLogger("ManipulateIPFSObjectUseCase")
    }

    suspend fun updateIPFSObjectContent(
        ipfsObject: IPFSObject,
        content: ByteArray,
        onlyLocally: Boolean = false,
        override: Boolean = false
    ): Either<ErrorEntity, IPFSObject> {
        LOGGER.info("Updating ipfsObject content.")
        return when (val createIPFSObjectResult =
            createIPFSObjectUseCase.create(
                content,
                ipfsObject.name,
                ipfsObject.type,
                ipfsObject.localPath,
                override,
                ipfsObject.previousVersionHash,
                ipfsObject.version,
                onlyLocally
            )) {
            is Either.Left -> createIPFSObjectResult
            is Either.Right -> {
                val newObject = createIPFSObjectResult.b
                when (val updatedParentsResult = replaceIPFSObjectForAllParents(ipfsObject, newObject, onlyLocally)) {
                    is Either.Left -> updatedParentsResult
                    is Either.Right -> {
                        when (val deleteObjectResult = deleteIPFSObject(ipfsObject, true)) {
                            is Either.Left -> deleteObjectResult
                            is Either.Right -> Either.Right(newObject)
                        }
                    }
                }
            }
        }
    }

    suspend fun renameIPFSObject(
        ipfsObject: IPFSObject,
        newName: String,
        onlyLocally: Boolean = false,
        override: Boolean = false
    ): Either<ErrorEntity, IPFSObject> {
        LOGGER.info("Renaming ipfsObject.")
        return try {
            when (val syncResult = syncIPFSObjectUseCase.syncObjectFromIPFS(ipfsObject)) {
                is Either.Left -> syncResult
                is Either.Right -> {
                    val objectWithContent = syncResult.b
                    val fileContent = localStorageUseCase.getObjectContent(objectWithContent)
                    when (val createIPFSObjectResult = createIPFSObjectUseCase.create(
                        fileContent,
                        newName,
                        objectWithContent.type,
                        objectWithContent.localPath,
                        override,
                        objectWithContent.previousVersionHash,
                        objectWithContent.version,
                        onlyLocally
                    )
                    ) {
                        is Either.Left -> createIPFSObjectResult
                        is Either.Right -> {
                            val newObject = createIPFSObjectResult.b
                            when (val updatedParentsResult = replaceIPFSObjectForAllParents(ipfsObject, newObject, onlyLocally)) {
                                is Either.Left -> updatedParentsResult
                                is Either.Right -> {
                                    when (val deleteObjectResult = deleteIPFSObject(ipfsObject, true)) {
                                        is Either.Left -> deleteObjectResult
                                        is Either.Right -> Either.Right(newObject)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError(e.message))
        }
    }

    suspend fun deleteIPFSObject(
        ipfsObject: IPFSObject,
        onlyLocally: Boolean = false,
        force: Boolean = false
    ): Either<ErrorEntity, Unit> {
        LOGGER.info("Deleting ipfsObject.")
        var error: Either.Left<ErrorEntity>? = null
        return try {
            if (onlyLocally) {
                if (ipfsObject.syncState != SyncState.UNSYNCED_ONLY_LOCAL || force) {
                    LOGGER.info("Deleting ipfsObject locally.")
                    val updatedObject = ipfsObject.copy(syncState = SyncState.UNSYNCED_ONLY_REMOTE, localPath = null)
                    when (val updatedIPFSObjectResult = ipfsObjectRepo.update(updatedObject)) {
                        is Either.Left -> error = updatedIPFSObjectResult
                    }
                } else {
                    error =
                        Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Can't delete ipfsObject locally as it is not synced remotely and therefore data would be lost. Forcing is disabled."))
                }
            } else {
                LOGGER.info("Deleting ipfsObject completely.")
                encryptionUseCase.deleteKeysForObject(ipfsObject)
                when (val deletedIPFSObjectResult = ipfsObjectRepo.delete(ipfsObject)) {
                    is Either.Left -> error = deletedIPFSObjectResult
                }
                // Deleting file from IPFS if it is not saved only locally
                if (ipfsObject.syncState != SyncState.UNSYNCED_ONLY_LOCAL) {
                    when (val deleteContentIPFSResult = ipfsUseCase.deleteFromIPFS(ipfsObject.contentHash)) {
                        is Either.Left -> error = deleteContentIPFSResult
                        is Either.Right -> {
                            if (!deleteContentIPFSResult.b) {
                                error = Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Deleting content from IPFS failed."))
                            }
                        }
                    }
                    when (val deleteMetaIPFSResult = ipfsUseCase.deleteFromIPFS(ipfsObject.metaHash)) {
                        is Either.Left -> error = deleteMetaIPFSResult
                        is Either.Right -> {
                            if (!deleteMetaIPFSResult.b) {
                                error = Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Deleting meta from IPFS failed."))
                            }
                        }
                    }
                }
            }
            if (error != null) {
                error
            } else {
                localStorageUseCase.deleteContentObject(ipfsObject)
                localStorageUseCase.deleteMetaObject(ipfsObject)
                deleteIPFSObjectFromAllParents(ipfsObject, onlyLocally)
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError(e.message))
        }
    }

    suspend fun deleteIPFSObjectFromAllParents(
        child: IPFSObject,
        onlyLocally: Boolean = false,
    ): Either<ErrorEntity, Unit> {
        return when (val parentsResult = ipfsObjectRepo.getParents(child.metaHash)) {
            is Either.Left -> parentsResult
            is Either.Right -> {
                var errorEntity: ErrorEntity? = null
                parentsResult.b.forEach parentForEach@{ parent ->
                    when (val updatedParentResult = updateChildObjectsUseCase.removeObject(parent, child, onlyLocally)) {
                        is Either.Left -> {
                            errorEntity = updatedParentResult.a
                            return@parentForEach
                        }
                        is Either.Right -> deleteIPFSObjectFromAllParents(updatedParentResult.b, onlyLocally)
                    }
                }
                return if (errorEntity == null) {
                    Either.Right(Unit)
                } else {
                    Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Removing object from parent failed."))
                }
            }
        }
    }

    suspend fun replaceIPFSObjectForAllParents(
        oldChild: IPFSObject,
        newChild: IPFSObject,
        onlyLocally: Boolean = false,
    ): Either<ErrorEntity, Unit> {
        return when (val parentsResult = ipfsObjectRepo.getParents(oldChild.metaHash)) {
            is Either.Left -> parentsResult
            is Either.Right -> {
                var errorEntity: ErrorEntity? = null
                parentsResult.b.forEach parentForEach@{ parent ->
                    when (val outerUpdatedParentResult = updateChildObjectsUseCase.removeObject(parent, oldChild, onlyLocally)) {
                        is Either.Left -> {
                            errorEntity = outerUpdatedParentResult.a
                            return@parentForEach
                        }
                        is Either.Right -> {
                            when (val innerUpdatedParentResult = updateChildObjectsUseCase.addObject(outerUpdatedParentResult.b, newChild, onlyLocally)) {
                                is Either.Left -> {
                                    errorEntity = innerUpdatedParentResult.a
                                    return@parentForEach
                                }
                                is Either.Right -> replaceIPFSObjectForAllParents(parent, innerUpdatedParentResult.b, onlyLocally)
                            }
                        }
                    }
                }
                return if (errorEntity == null) {
                    Either.Right(Unit)
                } else {
                    Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Replacing object from parent failed."))
                }
            }
        }
    }
}