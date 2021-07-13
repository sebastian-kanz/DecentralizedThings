package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectExportable
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType

internal class ShareIPFSObjectUseCase {

    fun isShared(file: IPFSObject): Boolean {
        return false
    }

    fun getSharedWithPubKeys(file: IPFSObject): List<String> {
        return listOf()
    }

    fun shareWithPubKey(pubkey: String, file: IPFSObject) {
    }

    suspend fun generateExportableObject(ipfsObject: IPFSObject): IPFSObjectExportable {
        return IPFSObjectExportable(
            metaHash = ipfsObject.metaHash,
            contentHash = ipfsObject.contentHash,
            previousVersionHash = ipfsObject.previousVersionHash,
            version = ipfsObject.version,
            type = ipfsObject.type,
            name = ipfsObject.name,
            timestamp = ipfsObject.timestamp,
            decryptedSize = ipfsObject.decryptedSize,
            contentIV = ipfsObject.contentIV,
            metaIV = ipfsObject.metaIV,
            objects = ipfsObject.objects,
            keys = emptyList(),
            rootKey = "",
        )
    }
}