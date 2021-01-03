package eth.sebastiankanz.decentralizedthings.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eth.sebastiankanz.decentralizedthings.data.model.EncryptionBundle

/**
 * The Data Access Object for the [EncryptionBundle] class.
 */

@Dao
interface EncryptionBundleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(encryptionBundle: EncryptionBundle): Long

    @Query(QUERY_BUNDLE_BY_HASH)
    fun get(hash: String): EncryptionBundle?

    @Update
    suspend fun update(track: EncryptionBundle)

    @Delete
    suspend fun delete(bundle: EncryptionBundle)

    companion object {
        private const val QUERY_ALL = "SELECT * FROM encryptionbundles ORDER BY id"
        private const val QUERY_BUNDLE_BY_HASH =
            "SELECT * FROM encryptionbundles WHERE ipfsHash = :hash LIMIT 1"
    }
}

