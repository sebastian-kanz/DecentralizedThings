package eth.sebastiankanz.decentralizedthings.data.repository

import androidx.lifecycle.LiveData
import eth.sebastiankanz.decentralizedthings.data.model.File

interface FileRepository {
    /**
     * Search a [Notification] by its ID.
     *
     * @param notificationId The ID of the Notification.
     * @return The [Notification] with the ID or null if no such notification exists.
     */
    fun getFile(hash: String): File?

    fun getByMetaHash(metaHash: String): File?

    fun observe(hash: String): LiveData<File?>

    fun observeByMetaHash(metaHash: String): LiveData<File?>

    fun create(file: File)

    fun observeAll(): LiveData<List<File>>

    fun getAll(): List<File>

    fun update(file: File)

    fun delete(file: File)

    fun exists(contentHash: String): Boolean

    fun existsByMetaHash(metaHash: String): Boolean

    fun observeNextVersion(metaHash: String): LiveData<File?>

    fun observePreviousVersion(previousVersionHash: String): LiveData<File?>

    fun getParents(metaHash: String): List<File>
}
