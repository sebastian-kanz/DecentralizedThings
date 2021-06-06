package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model

/**
 * This entity class represents a meta object on IPFS.
 * The class' attributes are serialized (json), encrypted and stored on IPFS.
 * The IPFS hash of this (encrypted!) file is saved in the [IPFSObject]'s [IPFSObject.metaHash] attribute.
 */

data class IPFSMetaObject(
    val contentHash: String,
    val previousVersionHash: String?,
    val version: Int,
    val name: String,
    val timestamp: Long,
    val decryptedSize: Long,
    val contentIV: ByteArray,
    val objects: List<Pair<String, String>>
)