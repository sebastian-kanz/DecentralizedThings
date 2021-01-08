package eth.sebastiankanz.decentralizedthings.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eth.sebastiankanz.decentralizedthings.data.model.File

/**
 * The Data Access Object for the [File] class.
 *
 */

@Dao
interface FileDao {
    @Query(QUERY_ALL)
    fun observeAll(): LiveData<List<File>>

    @Query(QUERY_ALL_LATEST)
    fun observeAllLatest(): LiveData<List<File>>

    @Query(QUERY_ALL)
    suspend fun getAll(): List<File>

    @Query(QUERY_BY_HASH)
    suspend fun get(fileHash: String): File

    @Query(QUERY_BY_META_HASH)
    suspend fun getByMetaHash(metaHash: String): File

    @Query(QUERY_EXISTS)
    suspend fun exists(fileHash: String): Boolean

    @Query(QUERY_EXISTS_BY_META_HASH)
    suspend fun existsByMetaHash(metaHash: String): Boolean

    @Query(QUERY_BY_HASH)
    fun observe(fileHash: String): LiveData<File?>

    @Query(QUERY_BY_META_HASH)
    fun observeByMetaHash(metaHash: String): LiveData<File?>

    @Query(QUERY_NEXT_VERSION)
    fun observeNextVersion(metaHash: String): LiveData<File?>

    @Query(QUERY_PREVIOUS_VERSION)
    fun observePreviousVersion(previousVersionHash: String): LiveData<File?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(file: File)

    @Update
    suspend fun update(file: File)

    @Delete
    suspend fun delete(file: File)

    @Query(QUERY_PARENTS)
    suspend fun getParents(metaHash: String): List<File>

    companion object {
        private const val QUERY_ALL = "SELECT * FROM files ORDER BY timestamp"
        //todo: sql query 'with recursive'
        private const val QUERY_ALL_LATEST = "SELECT * FROM files ORDER BY timestamp"
        private const val QUERY_BY_HASH =
            "SELECT * FROM files WHERE contentHash = :fileHash LIMIT 1"
        private const val QUERY_BY_META_HASH =
            "SELECT * FROM files WHERE metaHash = :metaHash LIMIT 1"
        private const val QUERY_EXISTS = "SELECT EXISTS(SELECT * FROM files WHERE contentHash = :fileHash)"
        private const val QUERY_EXISTS_BY_META_HASH = "SELECT EXISTS(SELECT * FROM files WHERE metaHash = :metaHash)"
        private const val QUERY_NEXT_VERSION = "SELECT * FROM files WHERE previousVersionHash = :metaHash"
        private const val QUERY_PREVIOUS_VERSION = "SELECT * FROM files WHERE metaHash = :previousVersionHash"
        private const val QUERY_PARENTS = "SELECT * FROM files WHERE files LIKE '%' + :metaHash + '%'"
    }
}

