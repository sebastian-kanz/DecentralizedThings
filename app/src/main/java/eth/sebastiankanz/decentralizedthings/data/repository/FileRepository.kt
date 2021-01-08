package eth.sebastiankanz.decentralizedthings.data.repository

import androidx.lifecycle.LiveData
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity

interface FileRepository {
    /**
     * Search a [Notification] by its ID.
     *
     * @param notificationId The ID of the Notification.
     * @return The [Notification] with the ID or null if no such notification exists.
     */
    suspend fun getFile(hash: String): Either<ErrorEntity, File>

    suspend fun getByMetaHash(metaHash: String): Either<ErrorEntity, File>

    fun observe(hash: String): LiveData<File?>

    fun observeByMetaHash(metaHash: String): LiveData<File?>

    suspend fun create(file: File): Either<ErrorEntity, Unit>

    fun observeAll(): LiveData<List<File>>

    suspend fun getAll(): Either<ErrorEntity, List<File>>

    suspend fun update(file: File): Either<ErrorEntity, Unit>

    suspend fun delete(file: File): Either<ErrorEntity, Unit>

    suspend fun exists(contentHash: String): Either<ErrorEntity, Boolean>

    suspend fun existsByMetaHash(metaHash: String): Either<ErrorEntity, Boolean>

    fun observeNextVersion(metaHash: String): LiveData<File?>

    fun observePreviousVersion(previousVersionHash: String): LiveData<File?>

    suspend fun getParents(metaHash: String): Either<ErrorEntity, List<File>>
}
