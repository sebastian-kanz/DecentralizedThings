package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import com.google.gson.Gson
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.base.helpers.onSuccess
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSMetaObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSObjectRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.extensions.getMetaFileNameFromFileName
import eth.sebastiankanz.decentralizedthings.ipfsstorage.extensions.isValidIPFSHash
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import java.time.Instant
import java.util.logging.Logger

internal class CreateIPFSObjectUseCase(
    private val ipfsObjectRepo: IPFSObjectRepository,
    private val ipfsUseCase: IPFSUseCase,
    private val localStorageUseCase: LocalStorageUseCase
) {
    companion object {
        private val LOGGER = Logger.getLogger("CreateIPFSObjectUseCase")
        private val ROOT_PREFIX = "ROOT_"
    }

    private val gson = Gson()

    /**
     * Creates a new [IPFSObject].
     * @param data content of the ipfsObject
     * @param name the name of the ipfsObject
     * @param path the local path of the ipfsObject (relative to context.getExternalFilesDir(null)), not containing ipfsObject name
     * @param override if true, the ipfsObject will be overwritten if existent
     * @param previousVersionHash ipfs hash of the previous version of the ipfsObject
     * @param previousVersionNumber version number of the previous version of the ipfsObject
     * @param onlyLocally if true, the ipfsObject will only be created locally and won't be uploaded to IPFS
     * @param objects only set if this ipfsObject is a directory. Contains all child elements as pairs of ipfs meta-hashes and the corresponding iv for decryption
     * @return LiveData object containing the ipfsObject (null if something went wrong)
     */
    suspend fun create(
        data: ByteArray,
        name: String,
        type: IPFSObjectType,
        path: String? = null,
        override: Boolean = false,
        previousVersionHash: String? = null,
        previousVersionNumber: Int = 0,
        onlyLocally: Boolean = false,
        objects: List<Pair<String, String>> = emptyList(),
    ): Either<ErrorEntity, IPFSObject> {
        return try {
            when (val typedRootResult = ipfsObjectRepo.getRootByType(type)) {
                is Either.Left -> return typedRootResult
                is Either.Right -> {
                    val adaptedPath = getAdaptedPath(path, type)
                    if (typedRootResult.b == null) {
                        //create Root for current type
                        when (val createRootResult = createInternal(ByteArray(0), getTypedRootName(type), IPFSObjectType.ROOT(type))) {
                            is Either.Left -> createRootResult
                            is Either.Right -> createInternal(
                                data,
                                name,
                                type,
                                adaptedPath,
                                override,
                                previousVersionHash,
                                previousVersionNumber,
                                onlyLocally,
                                objects
                            ).onSuccess {
                                if (isTypedRootPath(adaptedPath, type)) {
                                    updateTypedRoot(type)
                                }
                            }
                        }
                    } else {
                        createInternal(
                            data,
                            name,
                            type,
                            adaptedPath,
                            override,
                            previousVersionHash,
                            previousVersionNumber,
                            onlyLocally,
                            objects
                        ).onSuccess {
                            if (isTypedRootPath(adaptedPath, type)) {
                                updateTypedRoot(type)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.CreateObjectError(e.message))
        }
    }

    private fun getTypedRootName(type: IPFSObjectType): String {
        return ROOT_PREFIX + type::class.simpleName
    }

    private fun getAdaptedPath(path: String?, type: IPFSObjectType): String {
        return path?.let { getTypedRootName(type) + "/" + it } ?: getTypedRootName(type)
    }

    private fun isTypedRootPath(path: String, type: IPFSObjectType): Boolean {
        return path == getTypedRootName(type)
    }

    private fun updateTypedRoot(type: IPFSObjectType) {
        //todo
    }

    private suspend fun createInternal(
        data: ByteArray,
        name: String,
        type: IPFSObjectType,
        path: String? = null,
        override: Boolean = false,
        previousVersionHash: String? = null,
        previousVersionNumber: Int = 0,
        onlyLocally: Boolean = false,
        objects: List<Pair<String, String>> = emptyList(),
    ): Either<ErrorEntity, IPFSObject> {
        LOGGER.info("Creating object.")
        return try {
            localStorageUseCase.writeContent(
                IPFSObject(name = name, localPath = path),
                override,
                data
            )
            when (val contentUploadResult = ipfsUseCase.uploadToIPFS(data, onlyLocally, name)) {
                is Either.Left -> contentUploadResult
                is Either.Right -> {
                    val encryptionBundleContent = contentUploadResult.b
                    val contentHash = encryptionBundleContent.ipfsHash
                    val contentIV = encryptionBundleContent.initializationVector
                    if (contentHash.isValidIPFSHash()) {
                        val metaObject = IPFSMetaObject(
                            contentHash = contentHash,
                            previousVersionHash = previousVersionHash,
                            version = (previousVersionNumber + 1),
                            name = name,
                            timestamp = Instant.now().toEpochMilli(),
                            decryptedSize = data.size.toLong(),
                            contentIV = contentIV,
                            objects = objects
                        )
                        val metaObjectJSON = gson.toJson(metaObject)
                        localStorageUseCase.writeMetaData(
                            IPFSObject(
                                name = name,
                                localMetaPath = path
                            ), override, metaObjectJSON.toByteArray()
                        )
                        when (val metaUploadResult = ipfsUseCase.uploadToIPFS(metaObjectJSON.toByteArray(), onlyLocally, name.getMetaFileNameFromFileName())) {
                            is Either.Left -> metaUploadResult
                            is Either.Right -> {
                                val encryptionBundleMeta = metaUploadResult.b
                                val metaHash = encryptionBundleMeta.ipfsHash
                                val metaIV = encryptionBundleMeta.initializationVector
                                if (metaHash.isValidIPFSHash()) {
                                    val result = IPFSObject(
                                        contentHash = contentHash,
                                        metaHash = metaHash,
                                        previousVersionHash = previousVersionHash,
                                        version = (previousVersionNumber + 1),
                                        type = type,
                                        name = name,
                                        timestamp = metaObject.timestamp,
                                        decryptedSize = data.size.toLong(),
                                        syncState = if (onlyLocally) SyncState.UNSYNCED_ONLY_LOCAL else SyncState.SYNCED,
                                        localPath = path,
                                        localMetaPath = path,
                                        contentIV = contentIV,
                                        metaIV = metaIV,
                                        objects = objects
                                    )
                                    when (val createObjectResult = ipfsObjectRepo.create(result)) {
                                        is Either.Left -> createObjectResult
                                        is Either.Right -> Either.Right(result)
                                    }
                                } else {
                                    Either.Left(ErrorEntity.UseCaseError.CreateObjectError("$metaHash is not a valid IPFS hash."))
                                }
                            }
                        }
                    } else {
                        Either.Left(ErrorEntity.UseCaseError.CreateObjectError("$contentHash is not a valid IPFS hash."))
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.CreateObjectError(e.message))
        }
    }
}