package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model

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