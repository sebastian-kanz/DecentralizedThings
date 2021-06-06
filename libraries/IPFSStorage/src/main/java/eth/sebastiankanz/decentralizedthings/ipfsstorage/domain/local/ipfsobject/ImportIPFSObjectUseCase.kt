package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import com.google.gson.Gson
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSMetaObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSObjectRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import java.util.logging.Logger

internal class ImportIPFSObjectUseCase(
    private val ipfsObjectRepo: IPFSObjectRepository,
    private val ipfsUseCase: IPFSUseCase,
    private val syncIPFSObjectUseCase: SyncIPFSObjectUseCase
) {
    companion object {
        private val LOGGER = Logger.getLogger("ImportIPFSObjectUseCase")
    }

    private val gson = Gson()

    /**
     * Imports a [IPFSObject] from IPFS. Requires that the corresponding decryption key is already imported.
     * @param metaHash IPFS hash of the meta object containing all information about the object
     * @param metaIV IV for decrypting the meta data
     * @param path path to save the file locally. Is used to e.g. create local directory structures
     * @return LiveData object containing the object (null if something went wrong)
     */
    suspend fun importIPFSObjectFromIPFS(metaHash: String, metaIV: ByteArray, path: String? = null): Either<ErrorEntity, IPFSObject> {
        LOGGER.info("Importing object from IPFS.")
        return try {
            when (val objectResult = ipfsObjectRepo.getByMetaHash(metaHash)) {
                is Either.Left -> objectResult
                is Either.Right -> {
                    //Todo: why is here nothing done with the result in the next line?
                    val existingObject = objectResult.b
                    when (val downloadResult = ipfsUseCase.downloadFromIPFS(metaHash, metaIV)) {
                        is Either.Left -> downloadResult
                        is Either.Right -> {
                            val metaData = downloadResult.b
                            if (metaData.isNotEmpty()) {
                                val metaJSON = String(metaData)
                                val ipfsMetaObject = gson.fromJson(metaJSON, IPFSMetaObject::class.java)
                                LOGGER.info("Downloaded and parsed meta object json from IPFS: $ipfsMetaObject")
                                val ipfsObject = IPFSObject(
                                    contentHash = ipfsMetaObject.contentHash,
                                    metaHash = metaHash,
                                    previousVersionHash = ipfsMetaObject.previousVersionHash,
                                    version = ipfsMetaObject.version,
                                    // TODO: add type???
                                    name = ipfsMetaObject.name,
                                    timestamp = ipfsMetaObject.timestamp,
                                    decryptedSize = ipfsMetaObject.decryptedSize,
                                    syncState = SyncState.UNSYNCED_ONLY_REMOTE,
                                    localPath = null,
                                    localMetaPath = null,
                                    contentIV = ipfsMetaObject.contentIV,
                                    metaIV = metaIV
                                )
                                when (val createObjectResult = ipfsObjectRepo.create(ipfsObject)) {
                                    is Either.Left -> createObjectResult
                                    is Either.Right -> {
                                        when (val syncResult = syncIPFSObjectUseCase.syncObjectFromIPFS(ipfsObject)) {
                                            is Either.Left -> syncResult
                                            is Either.Right -> {
                                                Either.Right(syncResult.b)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Either.Left(ErrorEntity.UseCaseError.ImportObjectError("Received empty object from IPFS. Importing object from IPFS failed."))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ImportObjectError(e.message))
        }
    }
}