package eth.sebastiankanz.decentralizedthings.domain.local.file

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.data.repository.FileRepository
import eth.sebastiankanz.decentralizedthings.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.extensions.getMetaFileNameFromFileName
import java.util.logging.Logger

class SyncFileUseCase(
    private val fileRepo: FileRepository,
    private val ipfsUseCase: IPFSUseCase,
    private val localStorageUseCase: LocalStorageUseCase,
) {
    companion object {
        private val LOGGER = Logger.getLogger("SyncFileUseCase")
    }

    fun syncFileFromIPFS(file: File): LiveData<File?> {
        try {
            return MediatorLiveData<File>().apply {
                if (file.syncState != SyncState.SYNCED && (!localStorageUseCase.contentExistsLocally(file) || !localStorageUseCase.metaExistsLocally(file))) {
                    addSource(ipfsUseCase.downloadFromIPFS(file.contentHash, file.contentIV)) { contentData ->
                        if (contentData != null && contentData.isNotEmpty()) {
                            localStorageUseCase.writeContent(file, true, contentData)
                            addSource(ipfsUseCase.downloadFromIPFS(file.metaHash, file.metaIV)) { metaData ->
                                if (metaData != null && metaData.isNotEmpty()) {
                                    localStorageUseCase.writeMetaData(file, true, metaData)
                                    val updatedFile = file.copy(
                                        decryptedSize = contentData.size.toLong(),
                                        syncState = SyncState.SYNCED,
                                    )
                                    postValue(updatedFile)
                                    fileRepo.update(updatedFile)
                                } else {
                                    LOGGER.info("Trying to download meta data failed.")
                                    postValue(null)
                                }
                            }
                        } else {
                            LOGGER.info("Trying to download content failed.")
                            postValue(null)
                        }
                    }
                } else {
                    LOGGER.info("File synced and existing locally. Nothing changed.")
                    postValue(file)
                }
            }
        } catch (e: Exception) {
            LOGGER.warning("Could not sync file from IPFS: $e")
            return MediatorLiveData<File?>().apply { postValue(null) }
        }
    }

    fun syncFileToIPFS(file: File): LiveData<File?> {
        try {
            LOGGER.info("Syncing file content to IPFS.")
            val fileContent = localStorageUseCase.getFileContent(file)
            return MediatorLiveData<File?>().apply {
                addSource(ipfsUseCase.uploadToIPFS(fileContent, false, file.name)) { encryptionBundleContent ->
                    if (encryptionBundleContent != null) {
                        val contentHash = encryptionBundleContent.ipfsHash
                        val contentIV = encryptionBundleContent.initializationVector
                        val metaContent = localStorageUseCase.getFileMetaData(file)
                        val metaFileName = file.name.getMetaFileNameFromFileName()
                        addSource(ipfsUseCase.uploadToIPFS(metaContent, false, metaFileName)) { encryptionBundleMeta ->
                            if (encryptionBundleMeta != null) {
                                val metaHash = encryptionBundleMeta.ipfsHash
                                val metaIV = encryptionBundleMeta.initializationVector
                                val updatedFile = file.copy(
                                    contentHash = contentHash,
                                    contentIV = contentIV,
                                    metaHash = metaHash,
                                    metaIV = metaIV,
                                    syncState = SyncState.SYNCED
                                )
                                fileRepo.update(updatedFile)
                                postValue(file.copy())
                            } else {
                                LOGGER.warning("Syncing file to IPFS failed: Could not upload meta data.")
                                postValue(null)
                            }
                        }
                    } else {
                        LOGGER.warning("Syncing file to IPFS failed: Could not upload content.")
                        postValue(null)
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.warning("Could not sync file to IPFS: $e")
            return MediatorLiveData<File?>().apply { postValue(null) }
        }
    }
}