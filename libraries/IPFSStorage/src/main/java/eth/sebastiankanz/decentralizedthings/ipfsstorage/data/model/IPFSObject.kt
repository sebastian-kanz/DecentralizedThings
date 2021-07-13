package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Model and Entity class for a Object.
 * The object's content is stored on ipfs. The metadata (filename, timestamp, etc.) is stored in a separate, encrypted json file on ipfs.
 * Hashes are always hashes of encrypted data! Data is never stored unencrypted on IPFS!
 *
 * To restore an [IPFSObject] from IPFS one needs the [metaHash] and the corresponding [metaIV] - which are both never stored on IPFS -
 * (if this information is lost, the object can never be restored) to decrypt the [IPFSMetaObject] from IPFS.
 * Afterwards [IPFSMetaObject.contentHash] and [IPFSMetaObject.contentIV] can be used to get and decrypt the content of the object.
 * The other attributes of [IPFSMetaObject] can be used to restore the full [IPFSObject].
 */
@Entity(tableName = "objects")
data class IPFSObject(
    // [Not in IPFS] ipfs hash of the encrypted meta content
    @PrimaryKey
    val metaHash: String = "",
    // ipfs hash of the encrypted object content
    val contentHash: String = "",
    // meta hash of the previous version of this object, allows making a history
    val previousVersionHash: String? = null,
    // the version number of this object
    val version: Int = 1,
    // the type of this object
    val type: IPFSObjectType = IPFSObjectType.RAW,
    // the object name
    val name: String = "",
    // creation timestamp in unix format
    val timestamp: Long = 0L,
    // the size of the object in bytes
    val decryptedSize: Long = 0L,
    // [Not in IPFS] sync state of the object if it is downloaded from ipfs and decrypted
    var syncState: SyncState = SyncState.NONE,
    // [Not in IPFS] local path on device
    var localPath: String? = null,
    // [Not in IPFS] local path on device
    var localMetaPath: String? = null,
    // encryption initialization vector
    val contentIV: ByteArray = ByteArray(0),
    val metaIV: ByteArray = ByteArray(0),
    // list of child objects: Pair of meta hash and the corresponding initialization vector as hex string
    val objects: List<Pair<String, String>> = emptyList(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IPFSObject

        if (metaHash != other.metaHash) return false
        if (contentHash != other.contentHash) return false
        if (previousVersionHash != other.previousVersionHash) return false
        if (version != other.version) return false
        if (type != other.type) return false
        if (name != other.name) return false
        if (timestamp != other.timestamp) return false
        if (decryptedSize != other.decryptedSize) return false
        if (syncState != other.syncState) return false
        if (localPath != other.localPath) return false
        if (localMetaPath != other.localMetaPath) return false
        if (!contentIV.contentEquals(other.contentIV)) return false
        if (!metaIV.contentEquals(other.metaIV)) return false
        if (objects != other.objects) return false

        return true
    }

    override fun hashCode(): Int {
        var result = metaHash.hashCode()
        result = 31 * result + contentHash.hashCode()
        result = 31 * result + (previousVersionHash?.hashCode() ?: 0)
        result = 31 * result + version
        result = 31 * result + type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + decryptedSize.hashCode()
        result = 31 * result + syncState.hashCode()
        result = 31 * result + (localPath?.hashCode() ?: 0)
        result = 31 * result + (localMetaPath?.hashCode() ?: 0)
        result = 31 * result + contentIV.contentHashCode()
        result = 31 * result + metaIV.contentHashCode()
        result = 31 * result + objects.hashCode()
        return result
    }
}