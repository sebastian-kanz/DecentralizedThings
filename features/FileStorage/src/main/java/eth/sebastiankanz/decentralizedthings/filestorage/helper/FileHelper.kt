package eth.sebastiankanz.decentralizedthings.filestorage.helper

import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject

internal fun IPFSObject.toFile(): File {
    return File(
        metaHash, contentHash, version, name, timestamp, decryptedSize, parseSyncState(), localPath, emptyList()
    )
}

internal fun IPFSObject.toDirectory(allFiles: List<File>): File {
    return File(
        metaHash, contentHash, version, name, timestamp, decryptedSize, parseSyncState(), localPath, allFiles
    )
}


