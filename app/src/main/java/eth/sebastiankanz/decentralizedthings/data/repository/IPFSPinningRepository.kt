package eth.sebastiankanz.decentralizedthings.data.repository

import androidx.lifecycle.LiveData

interface IPFSPinningRepository {
    fun pinByHash(hash: String, pinName: String): LiveData<Boolean>
    fun unPinByHash(hash: String): LiveData<Boolean>
}