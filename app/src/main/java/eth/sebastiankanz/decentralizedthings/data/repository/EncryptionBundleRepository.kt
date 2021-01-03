package eth.sebastiankanz.decentralizedthings.data.repository

import eth.sebastiankanz.decentralizedthings.data.model.EncryptionBundle

interface EncryptionBundleRepository {

    suspend fun insertEncryptionBundle(keyName: String, iv: ByteArray): EncryptionBundle

    fun updateEncryptionBundle(updatedBundle: EncryptionBundle)

    suspend fun getBundle(hash: String): EncryptionBundle?

    fun deleteBundle(bundle: EncryptionBundle)

}
