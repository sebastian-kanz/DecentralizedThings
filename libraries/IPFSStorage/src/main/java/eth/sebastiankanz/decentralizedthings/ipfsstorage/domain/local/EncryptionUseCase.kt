package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local

import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.common.hash.Hashing
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.EncryptionDataBundle
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.EncryptionBundleRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.extensions.toHexString
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import java.security.KeyStore
import java.util.logging.Logger
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class EncryptionUseCase(
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

    suspend fun encryptData(data: ByteArray, dataName: String?): Either<ErrorEntity, EncryptionDataBundle> {
        LOGGER.info("Encrypting data.")
        return try {
            val keyName = Hashing.sha256().hashBytes(data + (dataName ?: "").toByteArray()).asBytes().toHexString()
            val cipher = getInitializedCipherForEncryption(keyName)
            val encryptedData = cipher.doFinal(data)
            EncryptionBundle(keyName = keyName, initializationVector = cipher.iv)
            return when (val encryptionBundleResult = encryptionBundleRepo.insertEncryptionBundle(keyName, cipher.iv)) {
                is Either.Left -> encryptionBundleResult
                is Either.Right -> {
                    val encryptionBundle = encryptionBundleResult.b
                    LOGGER.info("Data encrypted with iv ${cipher.iv.toHexString()} and EncryptionBundle updated.")
                    Either.Right(EncryptionDataBundle(encryptionBundle, encryptedData))
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.EncryptionError(e.message))
        }
    }

    suspend fun decryptData(encryptionDataBundle: EncryptionDataBundle): Either<ErrorEntity, ByteArray> {
        LOGGER.info("Decrypting data.")
        return try {
            val cipher =
                getInitializedCipherForDecryption(encryptionDataBundle.encryptionBundle.keyName, encryptionDataBundle.encryptionBundle.initializationVector)
            val decryptedData = cipher.doFinal(encryptionDataBundle.ciphertext)
            Either.Right(decryptedData)
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.EncryptionError(e.message))
        }
    }

    fun deleteKey(keyName: String): Either<ErrorEntity, Unit> {
        LOGGER.info("Deleting key.")
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null) // Keystore must be loaded before it can be accessed
            keyStore.deleteEntry(keyName)
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.EncryptionError(e.message))
        }
    }

    suspend fun updateExistingEncryptionBundle(encryptionBundle: EncryptionBundle): Either<ErrorEntity, Unit> {
        LOGGER.info("Updating existing EncryptionBundle.")
        return try {
            encryptionBundleRepo.updateEncryptionBundle(encryptionBundle)
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.EncryptionError(e.message))
        }
    }

    fun importKey(bundle: EncryptionBundle, hash: String) {
        //todo
    }

    suspend fun getDecryptionKey(hash: String): Either<ErrorEntity, ByteArray> {
        LOGGER.info("Getting decryption key.")
        return try {
            when (val keyBundleResult = encryptionBundleRepo.getBundle(hash)) {
                is Either.Left -> keyBundleResult
                is Either.Right -> {
                    when (val exportKeyResult = exportKey(keyBundleResult.b.keyName)) {
                        null -> Either.Left(ErrorEntity.UseCaseError.EncryptionError("No key found."))
                        else -> Either.Right(exportKeyResult)
                    }
                }
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.EncryptionError(e.message))
        }
    }

    suspend fun getDecryptionKeyName(hash: String): Either<ErrorEntity, String> {
        LOGGER.info("Getting decryption key name.")
        return try {
            when (val keyBundleResult = encryptionBundleRepo.getBundle(hash)) {
                is Either.Left -> keyBundleResult
                is Either.Right -> Either.Right(keyBundleResult.b.keyName)
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.EncryptionError(e.message))
        }
    }

    fun deleteKeysForObject(ipfsObject: IPFSObject) {
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

    private fun exportKey(keyName: String): ByteArray? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(keyName, null)?.let {
            LOGGER.info("Key found with name $keyName.")
            return it.encoded
        }
        LOGGER.info("No key found with name $keyName.")
        return null
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
}