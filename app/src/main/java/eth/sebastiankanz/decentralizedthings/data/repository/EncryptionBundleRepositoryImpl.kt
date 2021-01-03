package eth.sebastiankanz.decentralizedthings.data.repository

import eth.sebastiankanz.decentralizedthings.data.dao.EncryptionBundleDao
import eth.sebastiankanz.decentralizedthings.data.model.EncryptionBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class EncryptionBundleRepositoryImpl(
    private val encryptionBundleDao: EncryptionBundleDao
) : EncryptionBundleRepository {

    companion object {
        private val LOGGER = Logger.getLogger("EncryptionBundleRepository")
    }

    override suspend fun insertEncryptionBundle(keyName: String, iv: ByteArray): EncryptionBundle {
        return EncryptionBundle(encryptionBundleDao.insert(EncryptionBundle()), keyName = keyName, initializationVector = iv)
    }

    override fun updateEncryptionBundle(updatedBundle: EncryptionBundle) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                LOGGER.info("Updating Bundle: $updatedBundle")
                encryptionBundleDao.update(updatedBundle)
            }
        }
    }

    override suspend fun getBundle(hash: String): EncryptionBundle? {
        return encryptionBundleDao.get(hash)
    }

    override fun deleteBundle(bundle: EncryptionBundle) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                LOGGER.info("Deleting Bundle: $bundle")
                encryptionBundleDao.delete(bundle)
            }
        }
    }
}
