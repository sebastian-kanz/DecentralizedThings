package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSObjectRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.extensions.getMetaFileNameFromFileName
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import java.util.logging.Logger

internal class SyncIPFSObjectUseCase(
    private val ipfsObjectRepo: IPFSObjectRepository,
    private val ipfsUseCase: IPFSUseCase,
    private val localStorageUseCase: LocalStorageUseCase,
) {
    companion object {
        private val LOGGER = Logger.getLogger("SyncIPFSObjectUseCase")
    }

    suspend fun syncObjectFromIPFS(ipfsObject: IPFSObject): Either<ErrorEntity, IPFSObject> {
        LOGGER.info("Syncing object from IPFS.")
        return try {
            if (ipfsObject.syncState != SyncState.SYNCED && (!localStorageUseCase.contentExistsLocally(ipfsObject) || !localStorageUseCase.metaExistsLocally(
                    ipfsObject
                ))
            ) {
                when (val downloadContentResult = ipfsUseCase.downloadFromIPFS(ipfsObject.contentHash, ipfsObject.contentIV)) {
                    is Either.Left -> downloadContentResult
                    is Either.Right -> {
                        val contentData = downloadContentResult.b
                        if (contentData.isNotEmpty()) {
                            localStorageUseCase.writeContent(ipfsObject, true, contentData)
                            when (val downloadMetaResult = ipfsUseCase.downloadFromIPFS(ipfsObject.metaHash, ipfsObject.metaIV)) {
                                is Either.Left -> downloadMetaResult
                                is Either.Right -> {
                                    val metaData = downloadMetaResult.b
                                    if (metaData.isNotEmpty()) {
                                        localStorageUseCase.writeMetaData(ipfsObject, true, metaData)
                                        val updatedObject = ipfsObject.copy(
                                            decryptedSize = contentData.size.toLong(),
                                            syncState = SyncState.SYNCED,
                                        )
                                        when (val updatedResult = ipfsObjectRepo.update(updatedObject)) {
                                            is Either.Left -> updatedResult
                                            is Either.Right -> {
                                                Either.Right(updatedObject)
                                            }
                                        }
                                    } else {
                                        Either.Left(ErrorEntity.UseCaseError.SyncObjectError("Trying to download meta data failed."))
                                    }
                                }
                            }
                        } else {
                            Either.Left(ErrorEntity.UseCaseError.SyncObjectError("Trying to download content failed."))
                        }
                    }
                }
            } else {
                Either.Right(ipfsObject)
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.SyncObjectError(e.message))
        }
    }

    suspend fun syncObjectToIPFS(ipfsObject: IPFSObject): Either<ErrorEntity, IPFSObject> {
        LOGGER.info("Syncing object to IPFS.")
        return try {
            val objectContent = localStorageUseCase.getObjectContent(ipfsObject)
            when (val uploadContentResult = ipfsUseCase.uploadToIPFS(objectContent, false, ipfsObject.name)) {
                is Either.Left -> uploadContentResult
                is Either.Right -> {
                    val encryptionBundleContent = uploadContentResult.b
                    val contentHash = encryptionBundleContent.ipfsHash
                    val contentIV = encryptionBundleContent.initializationVector
                    val metaContent = localStorageUseCase.getObjectMetaData(ipfsObject)
                    val metaObjectName = ipfsObject.name.getMetaFileNameFromFileName()
                    when (val uploadMetaResult = ipfsUseCase.uploadToIPFS(metaContent, false, metaObjectName)) {
                        is Either.Left -> uploadMetaResult
                        is Either.Right -> {
                            val encryptionBundleMeta = uploadMetaResult.b
                            val metaHash = encryptionBundleMeta.ipfsHash
                            val metaIV = encryptionBundleMeta.initializationVector
                            val updatedObject = ipfsObject.copy(
                                contentHash = contentHash,
                                contentIV = contentIV,
                                metaHash = metaHash,
                                metaIV = metaIV,
                                syncState = SyncState.SYNCED
                            )
                            when (val updateObjectResult = ipfsObjectRepo.update(updatedObject)) {
                                is Either.Left -> updateObjectResult
                                is Either.Right -> Either.Right(updatedObject)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.SyncObjectError(e.message))
        }
    }
}