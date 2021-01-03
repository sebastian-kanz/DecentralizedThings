package eth.sebastiankanz.decentralizedthings.domain.local.file

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.data.repository.FileRepository
import eth.sebastiankanz.decentralizedthings.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.EncryptionUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.extensions.toHexString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class ManipulateFileUseCase(
    private val fileRepo: FileRepository,
    private val ipfsUseCase: IPFSUseCase,
    private val localStorageUseCase: LocalStorageUseCase,
    private val createFileUseCase: CreateFileUseCase,
    private val encryptionUseCase: EncryptionUseCase,
    private val syncFileUseCase: SyncFileUseCase
) {
    companion object {
        private val LOGGER = Logger.getLogger("ManipulateFileUseCase")
    }

    fun updateFileContent(file: File, content: ByteArray, updateRecursive: Boolean, onlyLocally: Boolean = false): LiveData<File?> {
        LOGGER.info("Updating file content.")
        return MediatorLiveData<File?>().apply {
            addSource(createFileUseCase.create(content, file.name, file.localPath, false, file.previousVersionHash, file.version, onlyLocally)) { newFile ->
                if (newFile != null) {
                    updateParents(newFile, updateRecursive, onlyLocally)
                    deleteFile(file, updateRecursive, true)
                } else {
                    LOGGER.warning("Could not update file's content: File creation failed.")
                }
                postValue(newFile)
            }
        }
    }

    fun renameFile(file: File, newName: String, updateRecursive: Boolean, onlyLocally: Boolean = false, override: Boolean = false): LiveData<File?> {
        LOGGER.info("Renaming file.")
        return MediatorLiveData<File?>().apply {
            addSource(syncFileUseCase.syncFileFromIPFS(file)) { fileWithContent ->
                if (fileWithContent != null) {
                    val localFile = localStorageUseCase.getContentFile(fileWithContent)
                    val fileContent = localFile.readBytes()
                    addSource(
                        createFileUseCase.create(
                            fileContent,
                            newName,
                            file.localPath,
                            override,
                            fileWithContent.metaHash,
                            fileWithContent.version,
                            onlyLocally
                        )
                    ) { newFile ->
                        if (newFile != null) {
                            updateParents(newFile, updateRecursive, onlyLocally)
                            deleteFile(file, updateRecursive, true)
                        } else {
                            LOGGER.warning("Could not rename file: File creation failed.")
                        }
                        postValue(newFile)
                    }
                } else {
                    LOGGER.warning("Could not rename file: File download failed.")
                    postValue(null)
                }
            }
        }
    }

    fun deleteFile(file: File, updateRecursive: Boolean, onlyLocally: Boolean = false) {
        try {
            if (onlyLocally && file.syncState != SyncState.UNSYNCED_ONLY_LOCAL) {
                LOGGER.info("Deleting file locally.")
                val updatedFile = file.copy(syncState = SyncState.UNSYNCED_ONLY_REMOTE, localPath = null)
                fileRepo.update(updatedFile)
            } else {
                LOGGER.info("Deleting file completely.")
                encryptionUseCase.deleteKeysForFile(file)
                fileRepo.delete(file)
                if(file.syncState != SyncState.UNSYNCED_ONLY_LOCAL) {
                    ipfsUseCase.deleteFromIPFS(file.contentHash)
                    ipfsUseCase.deleteFromIPFS(file.metaHash)
                }
            }
            localStorageUseCase.deleteContentFile(file)
            localStorageUseCase.deleteMetaFile(file)
            updateParents(file, updateRecursive, onlyLocally, true)
        } catch (e: NoSuchFileException) {
            LOGGER.warning("Can not delete file: File not found.")
        } catch (e: AccessDeniedException) {
            LOGGER.warning("Can not delete file: Access denied.")
        }
    }

    fun moveFile(file: File, path: String, updateRecursive: Boolean, onlyLocally: Boolean) {
//        try {
//            LOGGER.info("Moving file.")
//            val newPath = localStorageUseCase.moveLocalStorage(file, path, false)
//            val updatedFile = file.copy(localPath = newPath)
//            fileRepo.update(file)
//            updateParents(updatedFile, updateRecursive, onlyLocally)
//        } catch (e: FileAlreadyExistsException) {
//            LOGGER.warning("Can not move file: File already exists and override is false.")
//        } catch (e: NoSuchFileException) {
//            LOGGER.warning("Can not move file: Not found.")
//        }
    }

    fun updateParents(file: File, recursive: Boolean, onlyLocally: Boolean = false, isDeleted: Boolean = false) {
        LOGGER.info("Updating file parents.")
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val parents = fileRepo.getParents(file.metaHash)
                parents.forEach { parent ->
                    updateChild(parent, file, onlyLocally, isDeleted)
                    if (recursive) {
                        updateParents(parent, recursive, onlyLocally)
                    }
                }
            }
        }
    }

    fun updateChild(file: File, child: File, onlyLocally: Boolean, isDeleted: Boolean) {
        LOGGER.info("Updating file child.")
        val oldChild = file.files.find { it.first == child.previousVersionHash }
        val newChildren = file.files.toMutableList()
        newChildren.remove(oldChild)
        if (!isDeleted) {
            newChildren.add(Pair(child.metaHash, child.metaIV.toHexString()))
        }
        createFileUseCase.create(
            ByteArray(0),
            file.name,
            file.localPath,
            true,
            file.metaHash,
            file.version,
            onlyLocally,
            newChildren
        )
    }


    fun addFiles(file: File, filesToAdd: List<Pair<String, String>>, onlyLocally: Boolean) {
        LOGGER.info("Adding files to directory.")
        if (localStorageUseCase.getContentFile(file).isDirectory) {
            createFileUseCase.create(
                ByteArray(0),
                file.name,
                file.localPath,
                true,
                file.metaHash,
                file.version,
                onlyLocally,
                file.files.apply { toMutableList().addAll(filesToAdd) }
            )
        }
    }

    fun addFile(file: File, fileToAdd: Pair<String, String>, onlyLocally: Boolean) = addFiles(file, listOf(fileToAdd), onlyLocally)

    fun removeFiles(file: File, filesToRemove: List<Pair<String, String>>, onlyLocally: Boolean) {
        LOGGER.info("Removing files from directory.")
        if (localStorageUseCase.getContentFile(file).isDirectory) {
           createFileUseCase.create(
                ByteArray(0),
                file.name,
                file.localPath,
                true,
                file.metaHash,
                file.version,
                onlyLocally,
                file.files.apply { toMutableList().removeAll(filesToRemove) }
            )
        }
    }

    fun removeFile(file: File, fileToRemove: Pair<String, String>, onlyLocally: Boolean) = removeFiles(file, listOf(fileToRemove), onlyLocally)
}