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
import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity
import eth.sebastiankanz.decentralizedthings.helpers.flatMap
import eth.sebastiankanz.decentralizedthings.helpers.flatMapAsync
import eth.sebastiankanz.decentralizedthings.helpers.map
import eth.sebastiankanz.decentralizedthings.helpers.onFailure
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

    suspend fun updateFileContent(file: File, content: ByteArray, updateRecursive: Boolean, onlyLocally: Boolean = false): Either<ErrorEntity, File> {
        LOGGER.info("Updating file content.")
        return try {
            when (val createFileResult =
                createFileUseCase.create(content, file.name, file.localPath, false, file.previousVersionHash, file.version, onlyLocally)) {
                is Either.Left -> createFileResult
                is Either.Right -> {
                    val newFile = createFileResult.b
                    when (val updatedParentsResult = updateParents(newFile, updateRecursive, onlyLocally)) {
                        is Either.Left -> updatedParentsResult
                        is Either.Right -> {
                            when (val deleteFileResult = deleteFile(file, updateRecursive, true)) {
                                is Either.Left -> deleteFileResult
                                is Either.Right -> Either.Right(newFile)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateFileError(e.message))
        }
    }

    suspend fun renameFile(
        file: File,
        newName: String,
        updateRecursive: Boolean,
        onlyLocally: Boolean = false,
        override: Boolean = false
    ): Either<ErrorEntity, File> {
        LOGGER.info("Renaming file.")
        return try {
            when (val syncResult = syncFileUseCase.syncFileFromIPFS(file)) {
                is Either.Left -> syncResult
                is Either.Right -> {
                    val fileWithContent = syncResult.b
                    val localFile = localStorageUseCase.getContentFile(fileWithContent)
                    val fileContent = localFile.readBytes()
                    when (val createFileResult = createFileUseCase.create(
                        fileContent,
                        newName,
                        file.localPath,
                        override,
                        fileWithContent.metaHash,
                        fileWithContent.version,
                        onlyLocally
                    )
                    ) {
                        is Either.Left -> createFileResult
                        is Either.Right -> {
                            val newFile = createFileResult.b
                            updateParents(newFile, updateRecursive, onlyLocally).flatMapAsync {
                                deleteFile(file, updateRecursive, true)
                            }.map {
                                newFile
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateFileError(e.message))
        }
    }

    suspend fun deleteFile(file: File, updateRecursive: Boolean, onlyLocally: Boolean = false): Either<ErrorEntity, Unit> {
        LOGGER.info("Deleting file.")
        return try {
            if (onlyLocally && file.syncState != SyncState.UNSYNCED_ONLY_LOCAL) {
                LOGGER.info("Deleting file locally.")
                val updatedFile = file.copy(syncState = SyncState.UNSYNCED_ONLY_REMOTE, localPath = null)
                when (val updatedFileResult = fileRepo.update(updatedFile)) {
                    is Either.Left -> return updatedFileResult
                }
            } else {
                LOGGER.info("Deleting file completely.")
                encryptionUseCase.deleteKeysForFile(file)
                when (val deletedFileResult = fileRepo.delete(file)) {
                    is Either.Left -> return deletedFileResult
                }
                if (file.syncState != SyncState.UNSYNCED_ONLY_LOCAL) {
                    when (val deleteContentIPFSResult = ipfsUseCase.deleteFromIPFS(file.contentHash)) {
                        is Either.Left -> return deleteContentIPFSResult
                        is Either.Right -> {
                            if (!deleteContentIPFSResult.b) {
                                return Either.Left(ErrorEntity.UseCaseError.ManipulateFileError("Deleting content from IPFS failed."))
                            }
                        }
                    }
                    when (val deleteMeteIPFSResult = ipfsUseCase.deleteFromIPFS(file.metaHash)) {
                        is Either.Left -> return deleteMeteIPFSResult
                        is Either.Right -> {
                            if (!deleteMeteIPFSResult.b) {
                                return Either.Left(ErrorEntity.UseCaseError.ManipulateFileError("Deleting content from IPFS failed."))
                            }
                        }
                    }
                }
            }
            localStorageUseCase.deleteContentFile(file)
            localStorageUseCase.deleteMetaFile(file)
            updateParents(file, updateRecursive, onlyLocally, true)
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateFileError(e.message))
        }
    }

    suspend fun moveFile(file: File, path: String, updateRecursive: Boolean, onlyLocally: Boolean) {
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

    private suspend fun updateParents(file: File, recursive: Boolean, onlyLocally: Boolean = false, isDeleted: Boolean = false): Either<ErrorEntity, Unit> {
        LOGGER.info("Updating file parents.")
        return try {
            when (val parentsResult = fileRepo.getParents(file.metaHash)) {
                is Either.Left -> parentsResult
                is Either.Right -> {
                    var errorOnUpdate: ErrorEntity? = null
                    parentsResult.b.map { parent ->

                        updateChild(parent, file, onlyLocally, isDeleted).onFailure { errorOnUpdate = it }

                        if (recursive) {
                            updateParents(parent, recursive, onlyLocally).onFailure { errorOnUpdate = it }
                        }

                    }
                    if (errorOnUpdate != null) {
                        Either.Left(errorOnUpdate!!)
                    } else {
                        Either.Right(Unit)
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateFileError(e.message))
        }
    }

    private suspend fun updateChild(file: File, child: File, onlyLocally: Boolean, isDeleted: Boolean): Either<ErrorEntity, Unit> {
        LOGGER.info("Updating file child.")
        return try {
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
            ).flatMap { Either.Right(Unit) }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateFileError(e.message))
        }
    }

    suspend fun addFiles(file: File, filesToAdd: List<Pair<String, String>>, onlyLocally: Boolean): Either<ErrorEntity, Unit> {
        LOGGER.info("Adding files to directory.")
        return try {
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
                ).flatMap { Either.Right(Unit) }
            } else {
                Either.Left(ErrorEntity.UseCaseError.ManipulateFileError("Can not add files to non-directory"))
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateFileError(e.message))
        }
    }

    suspend fun addFile(file: File, fileToAdd: Pair<String, String>, onlyLocally: Boolean) = addFiles(file, listOf(fileToAdd), onlyLocally)

    suspend fun removeFiles(file: File, filesToRemove: List<Pair<String, String>>, onlyLocally: Boolean): Either<ErrorEntity, Unit> {
        LOGGER.info("Removing files from directory.")
        return try {
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
                ).flatMap { Either.Right(Unit) }
            } else {
                Either.Left(ErrorEntity.UseCaseError.ManipulateFileError("Can not remove files from non-directory"))
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateFileError(e.message))
        }
    }

    suspend fun removeFile(file: File, fileToRemove: Pair<String, String>, onlyLocally: Boolean) = removeFiles(file, listOf(fileToRemove), onlyLocally)
}