package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository

import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.dao.EncryptionBundleDao
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

internal class EncryptionBundleRepositoryImpl(
    private val encryptionBundleDao: EncryptionBundleDao
) : EncryptionBundleRepository {

    companion object {
        private val LOGGER = Logger.getLogger("EncryptionBundleRepository")
    }

    override suspend fun insertEncryptionBundle(keyName: String, iv: ByteArray): Either<ErrorEntity, EncryptionBundle> {
        LOGGER.info("Inserting Bundle for key: $keyName")
        return withContext(Dispatchers.IO) {
            try {
                val id = encryptionBundleDao.insert(EncryptionBundle())
                Either.Right(EncryptionBundle(id = id, keyName = keyName, initializationVector = iv))
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.EncryptionBundleError(e.message))
            }
        }
    }

    override suspend fun updateEncryptionBundle(updatedBundle: EncryptionBundle): Either<ErrorEntity, Unit> {
        LOGGER.info("Updating Bundle: $updatedBundle")
        return withContext(Dispatchers.IO) {
            try {
                encryptionBundleDao.update(updatedBundle)
                Either.Right(Unit)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.EncryptionBundleError(e.message))
            }
        }
    }

    override suspend fun getBundle(hash: String): Either<ErrorEntity, EncryptionBundle> {
        LOGGER.info("Getting bundle for hash: $hash")
        return withContext(Dispatchers.IO) {
            try {
                val encryptionBundle = encryptionBundleDao.get(hash)
                Either.Right(encryptionBundle)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.EncryptionBundleError(e.message))
            }
        }
    }

    override suspend fun deleteBundle(bundle: EncryptionBundle): Either<ErrorEntity, Unit> {
        LOGGER.info("Deleting bundle: $bundle")
        return withContext(Dispatchers.IO) {
            try {
                LOGGER.info("Deleting Bundle: $bundle")
                encryptionBundleDao.delete(bundle)
                Either.Right(Unit)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.EncryptionBundleError(e.message))
            }
        }
    }
}
