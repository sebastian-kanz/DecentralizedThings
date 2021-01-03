package eth.sebastiankanz.decentralizedthings.domain.local.file

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.gson.Gson
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.model.IPFSMetaFile
import eth.sebastiankanz.decentralizedthings.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.data.repository.FileRepository
import eth.sebastiankanz.decentralizedthings.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.extensions.getMetaFileNameFromFileName
import eth.sebastiankanz.decentralizedthings.extensions.isValidIPFSHash
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
    fun create(
        data: ByteArray,
        name: String,
        path: String? = null,
        override: Boolean = false,
        previousVersionHash: String? = null,
        previousVersionNumber: Int = 0,
        onlyLocally: Boolean = false,
        files: List<Pair<String, String>> = emptyList()
    ): LiveData<File?> {
        try {
            LOGGER.info("Creating file.")
            localStorageUseCase.writeContent(File(name = name, localPath = path), override, data)
            return MediatorLiveData<File?>().apply {
                addSource(ipfsUseCase.uploadToIPFS(data, onlyLocally, name)) { encryptionBundleContent ->
                    if (encryptionBundleContent != null) {
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
                            addSource(
                                ipfsUseCase.uploadToIPFS(
                                    metaFileJSON.toByteArray(),
                                    onlyLocally,
                                    name.getMetaFileNameFromFileName()
                                )
                            ) { encryptionBundleMeta ->
                                if (encryptionBundleMeta != null) {
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
                                        fileRepo.create(result)
                                        postValue(result)
                                    } else {
                                        LOGGER.warning("$metaHash is not a valid IPFS hash. Could not create file.")
                                        postValue(null)
                                    }
                                } else {
                                    LOGGER.warning("Uploading meta data to IPFS failed. Could not create file.")
                                    postValue(null)
                                }
                            }
                        } else {
                            LOGGER.warning("$contentHash is not a valid IPFS hash. Could not create file.")
                            postValue(null)
                        }
                    } else {
                        LOGGER.warning("Uploading content to IPFS failed. Could not create file.")
                        postValue(null)
                    }
                }
            }
        } catch (e: FileAlreadyExistsException) {
            LOGGER.warning("Could not create file: File Already exists.")
            return MediatorLiveData<File?>().apply { postValue(null) }
        }
    }
}