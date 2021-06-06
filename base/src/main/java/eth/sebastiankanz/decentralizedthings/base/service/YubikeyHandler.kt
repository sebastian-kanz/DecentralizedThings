package eth.sebastiankanz.decentralizedthings.base.service

import android.security.keystore.KeyProperties
import android.util.Base64
import com.yubico.yubikit.piv.PivSession
import com.yubico.yubikit.piv.Slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.Key
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.suspendCoroutine

const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
const val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"

/**
 *
 *
 * private val yubikit = YubiKitManager(this)
 * private val nfcConfiguration = NfcConfiguration()
 *
 * yubikit.startUsbDiscovery(UsbConfiguration()) { device ->
 *      lifecycleScope.launch(Dispatchers.Main) {
 *           getPin(this@MainActivity)?.let { pin ->
 *               device.requestConnection(SmartCardConnection::class.java) {
 *                   yubikeyHandler.yubikeyData = YubikeyHandler.YubikeyData(
 *                       pin = pin,
 *                       piv = PivSession(it.value)
 *                   )
 *               }
 *           }
 *       }
 *   }
 *
 *
 */

class YubikeyHandler {
    data class YubikeyData(
        val pin: String,
        val piv: PivSession
    ) {
        fun getPublicKey(): PublicKey {
            return piv.getCertificate(Slot.KEY_MANAGEMENT).publicKey
        }
    }

    // Use key wrapping!!!!
    // Cipher.WRAP_MODE
    // https://proandroiddev.com/secure-data-in-android-encrypting-large-data-dda256a55b36

    var yubikeyData: YubikeyData? = null

    suspend fun decryptKeyWithYubikey(wrappedKeyData: String) = suspendCoroutine<Key> { continuation ->
        GlobalScope.launch(Dispatchers.Main) {
            try {
                yubikeyData?.apply {
                    val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
                    val encryptedKeyData = Base64.decode(wrappedKeyData, Base64.DEFAULT)
                    cipher.init(Cipher.DECRYPT_MODE, getPublicKey())
                    piv.verifyPin(pin.toCharArray())
                    val decryptedData = piv.decrypt(Slot.KEY_MANAGEMENT, encryptedKeyData, cipher)
                    val key = SecretKeySpec(decryptedData, transformation);
                    continuation.resumeWith(Result.success(key))
                } ?: continuation.resumeWith(Result.failure(Exception("Yubikey session missing for decryption.")))
            } catch (e: Throwable) {
                continuation.resumeWith(Result.failure(e))
            }
        }
    }

    suspend fun encryptKeyWithYubikey(keyToBeEncrypted: Key) = suspendCoroutine<ByteArray> { continuation ->
        GlobalScope.launch(Dispatchers.Main) {
            try {
                yubikeyData?.apply {
                    val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
                    cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
                    val encryptedData = cipher.doFinal(keyToBeEncrypted.encoded)
                    continuation.resumeWith(Result.success(encryptedData))
                } ?: continuation.resumeWith(Result.failure(Exception("Yubikey public key missing for encryption.")))
            } catch (e: Throwable) {
                continuation.resumeWith(Result.failure(e))
            }
        }
    }

    suspend fun decryptWithYubikey(encryptedData: ByteArray) = suspendCoroutine<ByteArray> { continuation ->
        GlobalScope.launch(Dispatchers.Main) {
            try {
                yubikeyData?.apply {
                    val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
                    cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
                    piv.verifyPin(pin.toCharArray())
                    val decryptedData = piv.decrypt(Slot.KEY_MANAGEMENT, encryptedData, cipher)
                    continuation.resumeWith(Result.success(decryptedData))
                } ?: continuation.resumeWith(Result.failure(Exception("Yubikey session missing for decryption.")))
            } catch (e: Throwable) {
                continuation.resumeWith(Result.failure(e))
            }
        }
    }

    suspend fun encryptWithYubikey(data: ByteArray) = suspendCoroutine<ByteArray> { continuation ->
        GlobalScope.launch(Dispatchers.Main) {
            try {
                yubikeyData?.apply {
                    val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
                    cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
                    val encryptedData = cipher.doFinal(data)
                    continuation.resumeWith(Result.success(encryptedData))
                } ?: continuation.resumeWith(Result.failure(Exception("Yubikey public key missing for encryption.")))
            } catch (e: Throwable) {
                continuation.resumeWith(Result.failure(e))
            }
        }
    }
}