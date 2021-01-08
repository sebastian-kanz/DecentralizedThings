package eth.sebastiankanz.decentralizedthings.domain.local.file

import com.google.gson.Gson
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.model.IPFSMetaFile
import eth.sebastiankanz.decentralizedthings.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.data.repository.FileRepository
import eth.sebastiankanz.decentralizedthings.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.extensions.getMetaFileNameFromFileName
import eth.sebastiankanz.decentralizedthings.extensions.isValidIPFSHash
import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity
import java.util.logging.Logger

class CreateFileUseCase(
    private val fileRepo: FileRepository,
    private val ipfsUseCase: IPFSUseCase,
    private val localStorageUseCase: LocalStorageUseCase
) {
    companion object {
        private val LOGGER = Logger.getLogger("CreateFileUseCase")
        private const val META_PATH = ""
    }

    private val gson = Gson()

    /**
     * Creates a new [File].
     * @param data content of the file
     * @param name the name of the file
     * @param path the local path of the file (relative to context.getExternalFilesDir(null)), not containing file name
     * @param override if true, the file will be overwritten if existent
     * @param previousVersionHash ipfs hash of the previous version of the file
     * @param previousVersionNumber version number of the previous version of the file
     * @param onlyLocally if true, the file will only be created locally and won't be uploaded to IPFS
     * @param files only set if this file is a directory. Contains all child elements as pairs of ipfs meta-hashes and the corresponding iv for decryption
     * @return LiveData object containing the file (null if something went wrong)
     */
    suspend fun create(
        data: ByteArray,
        name: String,
        path: String? = null,
        override: Boolean = false,
        previousVersionHash: String? = null,
        previousVersionNumber: Int = 0,
        onlyLocally: Boolean = false,
        files: List<Pair<String, String>> = emptyList()
    ): Either<ErrorEntity, File> {
        LOGGER.info("Creating file.")
        return try {
            localStorageUseCase.writeContent(File(name = name, localPath = path), override, data)
            when(val contentUploadResult = ipfsUseCase.uploadToIPFS(data, onlyLocally, name)) {
                is Either.Left -> contentUploadResult
                is Either.Right -> {
                    val encryptionBundleContent = contentUploadResult.b
                    val contentHash = encryptionBundleContent.ipfsHash
                    val contentIV = encryptionBundleContent.initializationVector
                    if (contentHash.isValidIPFSHash()) {
                        val metaFile = IPFSMetaFile(
                            contentHash = contentHash,
                            previousVersionHash = previousVersionHash,
                            version = (previousVersionNumber + 1),
                            name = name,
                            timestamp = System.currentTimeMillis(),
                            decryptedSize = data.size.toLong(),
                            contentIV = contentIV,
                            files = files
                        )
                        val metaFileJSON = gson.toJson(metaFile)
                        localStorageUseCase.writeMetaData(File(name = name, localMetaPath = path), override, metaFileJSON.toByteArray())
                        when(val metaUploadResult = ipfsUseCase.uploadToIPFS(metaFileJSON.toByteArray(), onlyLocally, name.getMetaFileNameFromFileName())) {
                            is Either.Left -> metaUploadResult
                            is Either.Right -> {
                                val encryptionBundleMeta = metaUploadResult.b
                                val metaHash = encryptionBundleMeta.ipfsHash
                                val metaIV = encryptionBundleMeta.initializationVector
                                if (metaHash.isValidIPFSHash()) {
                                    val result = File(
                                        contentHash = contentHash,
                                        metaHash = metaHash,
                                        previousVersionHash = previousVersionHash,
                                        version = (previousVersionNumber + 1),
                                        name = name,
                                        timestamp = metaFile.timestamp,
                                        decryptedSize = data.size.toLong(),
                                        syncState = if (onlyLocally) SyncState.UNSYNCED_ONLY_LOCAL else SyncState.SYNCED,
                                        localPath = path,
                                        localMetaPath = path,
                                        contentIV = contentIV,
                                        metaIV = metaIV,
                                        files = files
                                    )
                                    when(val createFileResult = fileRepo.create(result)) {
                                        is Either.Left -> createFileResult
                                        is Either.Right -> Either.Right(result)
                                    }
                                } else {
                                    Either.Left(ErrorEntity.UseCaseError.CreateFileError("$metaHash is not a valid IPFS hash."))
                                }
                            }
                        }
                    } else {
                        Either.Left(ErrorEntity.UseCaseError.CreateFileError("$contentHash is not a valid IPFS hash."))
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.CreateFileError(e.message))
        }
    }
}