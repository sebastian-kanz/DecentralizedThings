package eth.sebastiankanz.decentralizedthings.domain.local.file

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.gson.Gson
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.model.IPFSMetaFile
import eth.sebastiankanz.decentralizedthings.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.data.repository.FileRepository
import eth.sebastiankanz.decentralizedthings.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity
import java.lang.Exception
import java.util.logging.Logger

class ImportFileUseCase(
    private val fileRepo: FileRepository,
    private val ipfsUseCase: IPFSUseCase,
    private val syncFileUseCase: SyncFileUseCase
) {
    companion object {
        private val LOGGER = Logger.getLogger("ImportFileUseCase")
    }

    private val gson = Gson()

    /**
     * Imports a [File] from IPFS. Requires that the corresponding decryption key is already imported.
     * @param metaHash IPFS hash of the meta file containing all information about the file
     * @param metaIV IV for decrypting the meta data
     * @param path path to save the file locally. Is used to e.g. create local directory structures
     * @return LiveData object containing the file (null if something went wrong)
     */
    suspend fun importFileFromIPFS(metaHash: String, metaIV: ByteArray, path: String? = null): Either<ErrorEntity, File> {
        LOGGER.info("Importing file from IPFS.")
        return try {
            when(val fileResult = fileRepo.getByMetaHash(metaHash)) {
                is Either.Left -> fileResult
                is Either.Right -> {
                    val existingFile = fileResult.b
                    when (val downloadResult = ipfsUseCase.downloadFromIPFS(metaHash, metaIV)) {
                        is Either.Left -> downloadResult
                        is Either.Right -> {
                            val metaData = downloadResult.b
                            if (metaData.isNotEmpty()) {
                                val metaJSON = String(metaData)
                                val metaFile = gson.fromJson(metaJSON, IPFSMetaFile::class.java)
                                LOGGER.info("Downloaded and parsed metaFile json from IPFS: $metaFile")
                                val file = File(
                                    contentHash = metaFile.contentHash,
                                    metaHash = metaHash,
                                    previousVersionHash = metaFile.previousVersionHash,
                                    version = metaFile.version,
                                    name = metaFile.name,
                                    timestamp = metaFile.timestamp,
                                    decryptedSize = metaFile.decryptedSize,
                                    syncState = SyncState.UNSYNCED_ONLY_REMOTE,
                                    localPath = null,
                                    localMetaPath = null,
                                    contentIV = metaFile.contentIV,
                                    metaIV = metaIV
                                )
                                when (val createFileResult = fileRepo.create(file)) {
                                    is Either.Left -> createFileResult
                                    is Either.Right -> {
                                        when (val syncResult = syncFileUseCase.syncFileFromIPFS(file)) {
                                            is Either.Left -> syncResult
                                            is Either.Right -> {
                                                Either.Right(syncResult.b)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Either.Left(ErrorEntity.UseCaseError.ImportFileError("Received empty file from IPFS. Importing file from IPFS failed."))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ImportFileError(e.message))
        }
    }
}