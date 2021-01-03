package eth.sebastiankanz.decentralizedthings.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.ipfs.api.IPFS
import io.ipfs.api.NamedStreamable
import io.ipfs.multihash.Multihash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class IPFSRepositoryImpl : IPFSRepository {

    companion object {
        private const val IPFS_GATEWAY = "/dnsaddr/ipfs.infura.io/tcp/5001/https"
        private val LOGGER = Logger.getLogger("IPFSRepository")
    }

    override fun upload(data: ByteArray, onlyHashCalculation: Boolean): LiveData<String> {
        val hash: MutableLiveData<String> = MutableLiveData()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    //private const val IPFS_GATEWAY = "/dns4/ipfs.io/tcp/80"
                    val ipfs = IPFS(IPFS_GATEWAY)
                    val tmpFile = NamedStreamable.ByteArrayWrapper("name_does_not_matter_for_upload.txt", data)
                    val multihash = ipfs.add(tmpFile, false, onlyHashCalculation).get(0).hash
                    hash.postValue(multihash.toString())
                    LOGGER.info("Uploaded data to IPFS: $multihash")
                } catch (e: java.lang.RuntimeException) {
                    LOGGER.warning("Uploading data to IPFS failed: ${e.message}")
                    hash.postValue("")
                }
            }
        }
        return hash
    }

    override fun download(hash: String): LiveData<ByteArray> {
        val data: MutableLiveData<ByteArray> = MutableLiveData()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                //private const val IPFS_GATEWAY = "/dns4/ipfs.io/tcp/80"
                try {
                    val ipfs = IPFS(IPFS_GATEWAY)
                    val multihash = Multihash.fromBase58(hash)
                    val content = ipfs.cat(multihash)
                    LOGGER.info("Downloaded data from IPFS: $content")
                    data.postValue(content)
                } catch (e: RuntimeException) {
                    LOGGER.warning("Downloading data from IPFS failed: ${e.message}")
                    data.postValue(ByteArray(0))
                }
            }
        }
        return data
    }
}
