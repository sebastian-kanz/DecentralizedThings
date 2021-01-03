package eth.sebastiankanz.decentralizedthings.domain.local.file

import eth.sebastiankanz.decentralizedthings.data.model.File

class ShareFileUseCase {

    fun isShared(file: File): Boolean {
        return false
    }

    fun getSharedWithPubKeys(file: File): List<String> {
        return listOf()
    }

    fun shareWithPubKey(pubkey: String, file: File) {
    }
}