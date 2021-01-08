package eth.sebastiankanz.decentralizedthings.domain.local.file

import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.data.repository.FileRepository
import eth.sebastiankanz.decentralizedthings.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.extensions.getMetaFileNameFromFileName
import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity
import java.util.logging.Logger

class SyncFileUseCase(
    private val fileRepo: FileRepository,
    private val ipfsUseCase: IPFSUseCase,
    private val localStorageUseCase: LocalStorageUseCase,
) {
    companion object {
        private val LOGGER = Logger.getLogger("SyncFileUseCase")
    }

    suspend fun syncFileFromIPFS(file: File): Either<ErrorEntity, File> {
        LOGGER.info("Syncing file from IPFS.")
        return try {
            if (file.syncState != SyncState.SYNCED && (!localStorageUseCase.contentExistsLocally(file) || !localStorageUseCase.metaExistsLocally(file))) {
                when(val downloadContentResult = ipfsUseCase.downloadFromIPFS(file.contentHash, file.contentIV)) {
                    is Either.Left -> downloadContentResult
                    is Either.Right -> {
                        val contentData = downloadContentResult.b
                        if (contentData.isNotEmpty()) {
                            localStorageUseCase.writeContent(file, true, contentData)
                            when(val downloadMetaResult = ipfsUseCase.downloadFromIPFS(file.metaHash, file.metaIV)) {
                                is Either.Left -> downloadMetaResult
                                is Either.Right -> {
                                    val metaData = downloadMetaResult.b
                                    if (metaData.isNotEmpty()) {
                                        localStorageUseCase.writeMetaData(file, true, metaData)
                                        val updatedFile = file.copy(
                                            decryptedSize = contentData.size.toLong(),
                                            syncState = SyncState.SYNCED,
                                        )
                                        when(val updatedResult = fileRepo.update(updatedFile)) {
                                            is Either.Left -> updatedResult
                                            is Either.Right -> {
                                                Either.Right(updatedFile)
                                            }
                                        }
                                    } else {
                                        Either.Left(ErrorEntity.UseCaseError.SyncFileError("Trying to download meta data failed."))
                                    }
                                }
                            }
                        } else {
                            Either.Left(ErrorEntity.UseCaseError.SyncFileError("Trying to download content failed."))
                        }
                    }
                }
            } else {
                Either.Right(file)
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.SyncFileError(e.message))
        }
    }

    suspend fun syncFileToIPFS(file: File): Either<ErrorEntity, File> {
        LOGGER.info("Syncing file to IPFS.")
        return try {
            val fileContent = localStorageUseCase.getFileContent(file)
            when(val uploadContentResult = ipfsUseCase.uploadToIPFS(fileContent, false, file.name)) {
                is Either.Left -> uploadContentResult
                is Either.Right -> {
                    val encryptionBundleContent = uploadContentResult.b
                    val contentHash = encryptionBundleContent.ipfsHash
                    val contentIV = encryptionBundleContent.initializationVector
                    val metaContent = localStorageUseCase.getFileMetaData(file)
                    val metaFileName = file.name.getMetaFileNameFromFileName()
                    when(val uploadMetaResult = ipfsUseCase.uploadToIPFS(metaContent, false, metaFileName)) {
                        is Either.Left -> uploadMetaResult
                        is Either.Right -> {
                            val encryptionBundleMeta = uploadMetaResult.b
                            val metaHash = encryptionBundleMeta.ipfsHash
                            val metaIV = encryptionBundleMeta.initializationVector
                            val updatedFile = file.copy(
                                contentHash = contentHash,
                                contentIV = contentIV,
                                metaHash = metaHash,
                                metaIV = metaIV,
                                syncState = SyncState.SYNCED
                            )
                            when(val updateFileResult = fileRepo.update(updatedFile)) {
                                is Either.Left -> updateFileResult
                                is Either.Right -> Either.Right(updatedFile)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.SyncFileError(e.message))
        }
    }
}