package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IPFSObjectExportable (
    val metaHash: String,
    val contentHash: String,
    val previousVersionHash: String? = null,
    val version: Int = 1,
    val type: IPFSObjectType = IPFSObjectType.RAW,
    val name: String = "",
    val timestamp: Long = 0L,
    val decryptedSize: Long = 0L,
    val contentIV: ByteArray,
    val metaIV: ByteArray,
    val objects: List<Pair<String, String>>,
    val keys: List<String>,
    val rootKey: String,
)