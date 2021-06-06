package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType

/**
 * The Data Access Object for the [IPFSObject] class.
 *
 */

@Dao
internal interface IPFSObjectDao {
    @Query(QUERY_ALL)
    fun observeAll(): LiveData<List<IPFSObject>>

    @Query(QUERY_ALL_LATEST)
    fun observeAllLatest(): LiveData<List<IPFSObject>>

    @Query(QUERY_ALL)
    suspend fun getAll(): List<IPFSObject>

    @Query(QUERY_BY_HASH)
    suspend fun get(objectHash: String): IPFSObject

    @Query(QUERY_BY_META_HASH)
    suspend fun getByMetaHash(metaHash: String): IPFSObject

    @Query(QUERY_BY_TYPE)
    suspend fun getByType(type: IPFSObjectType): List<IPFSObject>

    @Query(QUERY_EXISTS)
    suspend fun exists(objectHash: String): Boolean

    @Query(QUERY_EXISTS_BY_META_HASH)
    suspend fun existsByMetaHash(metaHash: String): Boolean

    @Query(QUERY_BY_HASH)
    fun observe(objectHash: String): LiveData<IPFSObject?>

    @Query(QUERY_BY_META_HASH)
    fun observeByMetaHash(metaHash: String): LiveData<IPFSObject?>

    @Query(QUERY_NEXT_VERSION)
    fun observeNextVersion(metaHash: String): LiveData<IPFSObject?>

    @Query(QUERY_PREVIOUS_VERSION)
    fun observePreviousVersion(previousVersionHash: String): LiveData<IPFSObject?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(obj: IPFSObject)

    @Update
    suspend fun update(obj: IPFSObject)

    @Delete
    suspend fun delete(obj: IPFSObject)

    @Query(QUERY_PARENTS)
    suspend fun getParents(metaHash: String): List<IPFSObject>

    companion object {
        private const val QUERY_ALL = "SELECT * FROM objects ORDER BY timestamp"
        private const val QUERY_ALL_LATEST = "SELECT * FROM objects ORDER BY timestamp"
        private const val QUERY_BY_HASH =
            "SELECT * FROM objects WHERE contentHash = :objectHash LIMIT 1"
        private const val QUERY_BY_META_HASH =
            "SELECT * FROM objects WHERE metaHash = :metaHash LIMIT 1"
        private const val QUERY_EXISTS = "SELECT EXISTS(SELECT * FROM objects WHERE contentHash = :objectHash)"
        private const val QUERY_EXISTS_BY_META_HASH = "SELECT EXISTS(SELECT * FROM objects WHERE metaHash = :metaHash)"
        private const val QUERY_NEXT_VERSION = "SELECT * FROM objects WHERE previousVersionHash = :metaHash"
        private const val QUERY_PREVIOUS_VERSION = "SELECT * FROM objects WHERE metaHash = :previousVersionHash"
        private const val QUERY_PARENTS = "SELECT * FROM objects WHERE objects LIKE '%' + :metaHash + '%'"
        private const val QUERY_BY_TYPE = "SELECT * FROM objects WHERE type = :type"
    }
}

