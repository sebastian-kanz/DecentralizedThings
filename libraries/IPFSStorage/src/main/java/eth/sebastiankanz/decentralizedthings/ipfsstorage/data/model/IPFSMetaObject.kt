package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model

import kotlinx.serialization.Serializable

/**
 * This entity class represents a meta object on IPFS.
 * The class' attributes are serialized (json), encrypted and stored on IPFS.
 * The IPFS hash of this (encrypted!) file is saved in the [IPFSObject]'s [IPFSObject.metaHash] attribute.
 */

@Serializable
data class IPFSMetaObject(
    val contentHash: String,
    val previousVersionHash: String?,
    val version: Int,
    val type: IPFSObjectType,
    val name: String,
    val timestamp: Long,
    val decryptedSize: Long,
    val contentIV: ByteArray,
    val objects: List<Pair<String, String>>
)