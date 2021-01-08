package eth.sebastiankanz.decentralizedthings.data.repository

import eth.sebastiankanz.decentralizedthings.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity

interface EncryptionBundleRepository {

    suspend fun insertEncryptionBundle(keyName: String, iv: ByteArray): Either<ErrorEntity, EncryptionBundle>

    suspend fun updateEncryptionBundle(updatedBundle: EncryptionBundle): Either<ErrorEntity, Unit>

    suspend fun getBundle(hash: String): Either<ErrorEntity, EncryptionBundle>

    suspend fun deleteBundle(bundle: EncryptionBundle): Either<ErrorEntity, Unit>
}
