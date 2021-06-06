package eth.sebastiankanz.decentralizedthings.base.features.filestorage.model

import eth.sebastiankanz.decentralizedthings.base.data.model.SyncState
import kotlinx.serialization.Serializable

@Serializable
data class File (
    val metaHash: String = "",
    val contentHash: String = "",
    val version: Int = 1,
    val name: String = "",
    val timestamp: Long = 0L,
    val size: Long = 0L,
    var syncState: SyncState = SyncState.NONE,
    var localPath: String? = null,
    var files: List<File> = emptyList(),
) {
    fun isDirectory(): Boolean{
        return files.isEmpty() && size == 0L
    }
}