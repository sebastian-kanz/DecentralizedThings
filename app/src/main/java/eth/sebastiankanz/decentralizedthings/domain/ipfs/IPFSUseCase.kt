package eth.sebastiankanz.decentralizedthings.domain.ipfs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import eth.sebastiankanz.decentralizedthings.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.data.model.EncryptionDataBundle
import eth.sebastiankanz.decentralizedthings.data.repository.IPFSPinningRepository
import eth.sebastiankanz.decentralizedthings.data.repository.IPFSRepository
import eth.sebastiankanz.decentralizedthings.domain.local.EncryptionUseCase
import eth.sebastiankanz.decentralizedthings.extensions.isValidIPFSHash
import eth.sebastiankanz.decentralizedthings.network.PinataClient
import java.util.logging.Logger

class IPFSUseCase(
    private val ipfsRepo: IPFSRepository,
    private val encryptionUseCase: EncryptionUseCase,
    private val pinataClient: PinataClient,
    private val ipfsPinningRepo: IPFSPinningRepository
) {

    companion object {
        private val LOGGER = Logger.getLogger("IPFSUseCase")
    }

    fun uploadToIPFS(
        data: ByteArray,
        onlyHashCalculation: Boolean = false,
        pinName: String? = null
    ): LiveData<EncryptionBundle?> {
        LOGGER.info("Uploading to IPFS.")
        return MediatorLiveData<EncryptionBundle>().apply {
            addSource(encryptionUseCase.encryptData(data, pinName)) { encryptionDataBundle ->
                addSource(ipfsRepo.upload(encryptionDataBundle.ciphertext, onlyHashCalculation)) { hash ->
                    if (hash.isValidIPFSHash()) {
                        LOGGER.info("Uploading to IPFS successful. Updating EncryptionBundle.")
                        val encryptionBundle = encryptionDataBundle.encryptionBundle.copy(ipfsHash = hash)
                        encryptionUseCase.updateExistingEncryptionBundle(encryptionBundle)
                        if (pinName != null) {
                            addSource(ipfsPinningRepo.pinByHash(hash, pinName)) { success ->
                                if (success) {
                                    postValue(encryptionBundle)
                                } else {
                                    LOGGER.warning("Uploading to IPFS failed: Pinning failed.")
                                    postValue(null)
                                }
                            }
                        } else {
                            postValue(encryptionBundle)
                        }
                    } else {
                        LOGGER.warning("Uploading to IPFS failed: IPFS hash not valid.")
                        postValue(null)
                    }
                }
            }
        }
    }

    fun uploadEncryptedToIPFS(encryptedContent: ByteArray, onlyHashCalculation: Boolean = false): LiveData<String> {
        LOGGER.info("Uploading encrypted to IPFS.")
        return ipfsRepo.upload(encryptedContent, onlyHashCalculation)
    }

    fun downloadFromIPFS(hash: String, iv: ByteArray): LiveData<ByteArray?> {
        LOGGER.info("Downloading from IPFS.")
        return MediatorLiveData<ByteArray>().apply {
            addSource(ipfsRepo.download(hash)) { encryptedData ->
                if (encryptedData.isNotEmpty()) {
                    addSource(encryptionUseCase.getDecryptionKeyName(hash)) { keyName ->
                        if (keyName.isNotEmpty() && keyName.isNotBlank()) {
                            LOGGER.info("Downloading from IPFS successful. Decrypting data.")
                            val encryptionDataBundle = EncryptionDataBundle(
                                EncryptionBundle(ipfsHash = hash, keyName = keyName, initializationVector = iv),
                                ciphertext = encryptedData
                            )
                            val decryptedData = encryptionUseCase.decryptData(encryptionDataBundle)
                            LOGGER.info("Data decrypted.")
                            postValue(decryptedData)
                        } else {
                            LOGGER.warning("No keyName for hash found.")
                            postValue(null)
                        }
                    }
                } else {
                    LOGGER.warning("Downloading decrypted from IPFS failed. Content is empty.")
                    postValue(null)
                }
            }
        }
    }

    fun downloadDecryptedFromIPFS(hash: String): LiveData<ByteArray?> {
        LOGGER.info("Downloading decrypted from IPFS.")
        return MediatorLiveData<ByteArray>().apply {
            addSource(ipfsRepo.download(hash)) { data ->
                if (data.isEmpty()) {
                    LOGGER.warning("Downloading decrypted from IPFS failed. Content is empty.")
                    postValue(null)
                } else {
                    LOGGER.info("Decrypted data downloaded.")
                    postValue(data)
                }
            }
        }
    }

    fun deleteFromIPFS(hash: String) {
        LOGGER.info("Deleting from IPFS.")
        ipfsPinningRepo.unPinByHash(hash)
    }
}