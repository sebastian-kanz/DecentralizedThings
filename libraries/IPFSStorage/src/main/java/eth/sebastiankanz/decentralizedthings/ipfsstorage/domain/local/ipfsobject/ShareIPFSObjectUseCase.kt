package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject

internal class ShareIPFSObjectUseCase {

    fun isShared(file: IPFSObject): Boolean {
        return false
    }

    fun getSharedWithPubKeys(file: IPFSObject): List<String> {
        return listOf()
    }

    fun shareWithPubKey(pubkey: String, file: IPFSObject) {
    }
}