package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository

import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity

internal interface EncryptionBundleRepository {

    suspend fun insertEncryptionBundle(keyName: String, iv: ByteArray): Either<ErrorEntity, EncryptionBundle>

    suspend fun updateEncryptionBundle(updatedBundle: EncryptionBundle): Either<ErrorEntity, Unit>

    suspend fun getBundle(hash: String): Either<ErrorEntity, EncryptionBundle>

    suspend fun deleteBundle(bundle: EncryptionBundle): Either<ErrorEntity, Unit>
}
