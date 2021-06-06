package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.Sharing

/**
 * The Data Access Object for the [Sharing] class.
 *
 */

@Dao
internal interface SharingDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(encryptionBundle: EncryptionBundle): Long

    @Query(QUERY_KEY_BY_HASH)
    fun get(hash: String): EncryptionBundle?

    @Query(QUERY_ALL)
    fun getAll(): List<Sharing>


    companion object {
        private const val QUERY_ALL = "SELECT * FROM sharings ORDER BY id"
        private const val QUERY_KEY_BY_HASH =
            "SELECT * FROM encyptionkeys WHERE ipfsHash = :hash LIMIT 1"
    }
}

