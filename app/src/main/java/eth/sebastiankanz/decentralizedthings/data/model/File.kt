package eth.sebastiankanz.decentralizedthings.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model and Entity class for a File.
 * The file's content is stored on ipfs. The metadata (filename, timestamp, etc.) is stored in a separate, encrypted json file on ipfs.
 *
 */

@Entity(tableName = "files")
data class File(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    // ipfs hash of the encrypted file content
    val contentHash: String = "",
    // [Not in IPFS]
    val metaHash: String = "",
    // meta hash of the previous version of this file, allows making a history
    val previousVersionHash: String? = null,
    // the version number of this container
    val version: Int = 1,
    // the filename
    val name: String = "",
    // creation timestamp in unix format
    val timestamp: Long = 0L,
    // the size of the file in bytes
    val decryptedSize: Long = 0L,
    // [Not in IPFS] sync state of the file if it is downloaded from ipfs and decrypted
    var syncState: SyncState = SyncState.NONE,
    // [Not in IPFS] local path on device
    var localPath: String? = null,
    // [Not in IPFS] local path on device
    var localMetaPath: String? = null,
    // encryption initialization vector
    val contentIV: ByteArray = ByteArray(0),
    val metaIV: ByteArray = ByteArray(0),
    // list of hashes of encryped metadata files and the corresponding initialization vector as hex string
    val files: List<Pair<String, String>> = emptyList(),
)

data class IPFSMetaFile(
    val contentHash: String,
    val previousVersionHash: String?,
    val version: Int,
    val name: String,
    val timestamp: Long,
    val decryptedSize: Long,
    val contentIV: ByteArray,
    val files: List<Pair<String, String>>
)

enum class SyncState(val state: Int) {
    // default, state not set
    NONE(0),

    // The file(s) exist(s) locally and on ipfs
    SYNCED(1),

    // The file(s) exist(s) only remotely on ipfs
    UNSYNCED_ONLY_REMOTE(2),

    // The file(s) exist(s) only locally
    UNSYNCED_ONLY_LOCAL(3),

    // The file(s) exist(s) only locally
    UNSYNCED_ONLY_PARTLY(4)
}

/*
JSON file in ipfs
hash is the ipfs hash of this file's content and metaHash is the ipfs hash of this json
{
    "contentHash":"Qmc5gCcjYypU7y28oCALwfSvxCBskLuPKWpK4qpterKC7z",
    "previousVersionHash":null,
    "version":1,
    "fileName":"testname",
    "timestamp": 12345678,
    "decryptedSize": 2048,
    "iv": "68616c6c6f2077656c74",
    "containerIVTuples":[
        {
            "metaHash":"Qmc5gCcjYypU7y28oCALwfSvxCBskLuPKWpK4qpterKC7z",
            "iv":"68616c6c6f2077656c74"
        },
        {
            "metaHash":"Qmc5gCcjYypU7y28oCALwfSvxCBskLuPKWpK4qpterKC7z",
            "iv":"68616c6c6f2077656c74"
        },
    ]
}*/