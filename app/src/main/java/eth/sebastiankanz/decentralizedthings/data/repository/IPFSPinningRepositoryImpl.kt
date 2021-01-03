package eth.sebastiankanz.decentralizedthings.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eth.sebastiankanz.decentralizedthings.network.PinataClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class IPFSPinningRepositoryImpl(
    private val pinataClient: PinataClient
): IPFSPinningRepository {

    companion object {
        private val LOGGER = Logger.getLogger("IPFSPinningRepository")
        private const val HTTP_OK = "OK"
    }

    override fun pinByHash(hash: String, pinName: String): LiveData<Boolean> {
        val pinningSuccess = MutableLiveData<Boolean>()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val pinataResponse = pinataClient.pinByHash(hash, pinName)
                if(pinataResponse.id != "") {
                    pinningSuccess.postValue(true)
                } else {
                    pinningSuccess.postValue(false)
                }
            }
        }
        return pinningSuccess
    }

    override fun unPinByHash(hash: String): LiveData<Boolean> {
        val unpinningSuccess = MutableLiveData<Boolean>()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val pinataResponse = pinataClient.unPinByHash(hash)
                if(pinataResponse.string() == HTTP_OK) {
                    unpinningSuccess.postValue(true)
                } else {
                    unpinningSuccess.postValue(false)
                }
            }
        }
        return unpinningSuccess
    }
}