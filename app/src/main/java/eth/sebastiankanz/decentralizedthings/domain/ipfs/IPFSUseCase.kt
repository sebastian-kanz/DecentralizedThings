package eth.sebastiankanz.decentralizedthings.domain.ipfs

import eth.sebastiankanz.decentralizedthings.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.data.model.EncryptionDataBundle
import eth.sebastiankanz.decentralizedthings.data.repository.IPFSPinningRepository
import eth.sebastiankanz.decentralizedthings.data.repository.IPFSRepository
import eth.sebastiankanz.decentralizedthings.domain.local.EncryptionUseCase
import eth.sebastiankanz.decentralizedthings.extensions.isValidIPFSHash
import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity
import java.util.logging.Logger

class IPFSUseCase(
    private val ipfsRepo: IPFSRepository,
    private val encryptionUseCase: EncryptionUseCase,
    private val ipfsPinningRepo: IPFSPinningRepository
) {

    companion object {
        private val LOGGER = Logger.getLogger("IPFSUseCase")
    }

    suspend fun uploadToIPFS(
        data: ByteArray,
        onlyHashCalculation: Boolean = false,
        pinName: String? = null
    ): Either<ErrorEntity, EncryptionBundle> {
        LOGGER.info("Uploading to IPFS.")
        return try {
            when (val encryptionDataBundleResult = encryptionUseCase.encryptData(data, pinName)) {
                is Either.Left -> return encryptionDataBundleResult
                is Either.Right -> {
                    when (val uploadResult = ipfsRepo.upload(encryptionDataBundleResult.b.ciphertext, onlyHashCalculation)) {
                        is Either.Left -> uploadResult
                        is Either.Right -> {
                            val hash = uploadResult.b
                            if (hash.isValidIPFSHash()) {
                                LOGGER.info("Uploading to IPFS successful. Updating EncryptionBundle.")
                                val encryptionBundle = encryptionDataBundleResult.b.encryptionBundle.copy(ipfsHash = hash)
                                encryptionUseCase.updateExistingEncryptionBundle(encryptionBundle)
                                if (pinName != null) {
                                    return when (val pinningResult = ipfsPinningRepo.pinByHash(hash, pinName)) {
                                        is Either.Left -> pinningResult
                                        is Either.Right -> {
                                            if (pinningResult.b) {
                                                Either.Right(encryptionBundle)
                                            } else {
                                                Either.Left(ErrorEntity.UseCaseError.UploadToIPFSFailed("Pinning failed."))
                                            }
                                        }
                                    }
                                } else {
                                    Either.Right(encryptionBundle)
                                }
                            } else {
                                Either.Left(ErrorEntity.UseCaseError.UploadToIPFSFailed("IPFS hash not valid."))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.UploadToIPFSFailed(e.message))
        }
    }

    suspend fun downloadFromIPFS(hash: String, iv: ByteArray): Either<ErrorEntity, ByteArray> {
        LOGGER.info("Downloading from IPFS.")
        return try {
            when (val downloadResult = ipfsRepo.download(hash)) {
                is Either.Left -> downloadResult
                is Either.Right -> {
                    val encryptedData = downloadResult.b
                    if (encryptedData.isNotEmpty()) {
                        when (val keyNameResult = encryptionUseCase.getDecryptionKeyName(hash)) {
                            is Either.Left -> keyNameResult
                            is Either.Right -> {
                                val keyName = keyNameResult.b
                                if (keyName.isNotEmpty() && keyName.isNotBlank()) {
                                    val encryptionDataBundle = EncryptionDataBundle(
                                        EncryptionBundle(ipfsHash = hash, keyName = keyName, initializationVector = iv),
                                        ciphertext = encryptedData
                                    )
                                    when (val decryptedDataResult = encryptionUseCase.decryptData(encryptionDataBundle)) {
                                        is Either.Left -> decryptedDataResult
                                        is Either.Right -> Either.Right(decryptedDataResult.b)
                                    }
                                } else {
                                    Either.Left(ErrorEntity.UseCaseError.DownloadFromIPFSFailed("No keyName for hash found."))
                                }
                            }
                        }
                    } else {
                        Either.Left(ErrorEntity.UseCaseError.DownloadFromIPFSFailed("Content is empty."))
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.DownloadFromIPFSFailed(e.message))
        }
    }

    suspend fun deleteFromIPFS(hash: String): Either<ErrorEntity, Boolean> {
        LOGGER.info("Deleting from IPFS.")
        return ipfsPinningRepo.unPinByHash(hash)
    }
}