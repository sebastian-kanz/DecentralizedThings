package eth.sebastiankanz.decentralizedthings.domain.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.hash.Hashing
import eth.sebastiankanz.decentralizedthings.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.data.model.EncryptionDataBundle
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.repository.EncryptionBundleRepository
import eth.sebastiankanz.decentralizedthings.extensions.toHexString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.util.logging.Logger
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionUseCase(
    private val encryptionBundleRepo: EncryptionBundleRepository
) {
    companion object {
        private val LOGGER = Logger.getLogger("EncryptionUseCase")
        private const val KEY_SIZE: Int = 256
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    }

    fun encryptData(data: ByteArray, dataName: String?): LiveData<EncryptionDataBundle> {
        LOGGER.info("Encrypting data.")
        val encryptedLiveData: MutableLiveData<EncryptionDataBundle> = MutableLiveData()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val keyName = Hashing.sha256().hashBytes(data + (dataName?: "").toByteArray()).asBytes().toHexString()
                val cipher = getInitializedCipherForEncryption(keyName)
                val encryptedData = cipher.doFinal(data)
                EncryptionBundle(keyName = keyName, initializationVector = cipher.iv)
                val encryptionBundle = encryptionBundleRepo.insertEncryptionBundle(keyName, cipher.iv)
                LOGGER.info("Data encrypted with iv ${cipher.iv.toHexString()} and EncryptionBundle updated.")
                encryptedLiveData.postValue(EncryptionDataBundle(encryptionBundle, encryptedData))
            }
        }
        return encryptedLiveData
    }

    fun decryptData(encryptionDataBundle: EncryptionDataBundle): ByteArray {
        LOGGER.info("Decrypting data with iv ${encryptionDataBundle.encryptionBundle.initializationVector.toHexString()}.")
        val cipher =
            getInitializedCipherForDecryption(encryptionDataBundle.encryptionBundle.keyName, encryptionDataBundle.encryptionBundle.initializationVector)
        return cipher.doFinal(encryptionDataBundle.ciphertext)
    }

    fun deleteKey(keyName: String) {
        LOGGER.info("Deleting key.")
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.deleteEntry(keyName)
    }

    private fun getInitializedCipherForEncryption(keyName: String): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    private fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(keyName, null)?.let {
            LOGGER.info("Key found.")
            return it as SecretKey
        }
        LOGGER.info("No key found. Generating new key.")

        // if you reach here, then a new SecretKey must be generated for that keyName
        val paramsBuilder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
//            setUserAuthenticationRequired(true)
            setUserAuthenticationRequired(false)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    fun updateExistingEncryptionBundle(encryptionBundle: EncryptionBundle) {
        LOGGER.info("Updating existing EncryptionBundle.")
        encryptionBundleRepo.updateEncryptionBundle(encryptionBundle)
    }

    fun importKey(bundle: EncryptionBundle, hash: String) {
        //todo
    }

    fun getDecryptionKey(hash: String): LiveData<ByteArray?> {
        val keyLD = MutableLiveData<ByteArray?>()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                var keyName = encryptionBundleRepo.getBundle(hash)?.keyName
                if(keyName == null) {
                    keyName = ""
                    LOGGER.warning("No key found for $hash.")
                }
                LOGGER.info("KeyName $keyName found for $hash.")
                keyLD.postValue(exportKey(keyName))
            }
        }
        return keyLD
    }

    fun getDecryptionKeyName(hash: String): LiveData<String> {
        val keyNameLD = MutableLiveData<String>()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                var keyName = encryptionBundleRepo.getBundle(hash)?.keyName
                if(keyName == null) {
                    keyName = ""
                    LOGGER.warning("No key found for $hash.")
                }
                LOGGER.info("KeyName $keyName found for $hash.")
                keyNameLD.postValue(keyName)
            }
        }
        return keyNameLD
    }

    fun deleteKeysForFile(file: File) {
        //todo
//        GlobalScope.launch {
//            withContext(Dispatchers.IO) {
//                val contentKey = encryptionBundleRepo.getKey(file.contentHash)
//                val metaKey = encryptionBundleRepo.getKey(file.metaHash)
//                if (contentKey != null && metaKey != null) {
//                    encryptionBundleRepo.deleteKey(contentKey)
//                    encryptionBundleRepo.deleteKey(metaKey)
//                } else {
//                    Log.e(TAG, "EncryptionKey not found for deletion!")
//                }
//            }
//        }
    }

    fun exportKey(keyName: String): ByteArray? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(keyName, null)?.let {
            LOGGER.info("Key found with name $keyName.")
            return it.encoded
        }
        LOGGER.info("No key found with name $keyName.")
        return null
    }
}