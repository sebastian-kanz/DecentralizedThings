package eth.sebastiankanz.decentralizedthings.data.repository

import androidx.lifecycle.LiveData

interface IPFSRepository {
    fun upload(data: ByteArray, onlyHashCalculation: Boolean): LiveData<String>
    fun download(hash: String): LiveData<ByteArray>
}
