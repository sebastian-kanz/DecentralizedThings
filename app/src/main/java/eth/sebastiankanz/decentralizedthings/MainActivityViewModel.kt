package eth.sebastiankanz.decentralizedthings

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eth.sebastiankanz.decentralizedthings.domain.local.file.GetFileUseCase
import io.ipfs.api.IPFS
import io.ipfs.api.NamedStreamable
import io.ipfs.cid.Cid
import io.ipfs.multihash.Multihash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class MainActivityViewModel(
    private val context: Context
) : ViewModel(), KoinComponent {

    val fileData: MutableLiveData<ByteArray> = MutableLiveData<ByteArray>()

    private val fileUseCase by inject<GetFileUseCase>()

    val files = fileUseCase.observeAll()

    private fun getRandomString(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return (1..20)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun doIPFSStuff() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                //val ipfs = IPFS("/dns4/ipfs.io/tcp/80")
                val ipfs = IPFS("/dns4/ipfs.infura.io/tcp/8080/https")
                //val ipfs = IPFS("/ip4/192.168.2.208/tcp/8080")
                val data: ByteArray = ipfs.dag.get(
                    Cid.decode("bafyreib53ttb2ea2iyds4xpcqgvyf5q6qkmhjajjxb5zgq4ygpsxei6lk4")
                )
                Log.i("test123", "data: " + String(data, Charsets.UTF_8))

                val data2: ByteArray = ipfs.cat(Multihash.fromBase58("Qmc5gCcjYypU7y28oCALwfSvxCBskLuPKWpK4qpterKC7z"))
                Log.i("test123", "data: " + String(data2, Charsets.UTF_8))

                //val data3: ByteArray = ipfs.cat(Multihash.fromBase58("QmczcYpf51WsjeSUsoMmNXKcUtk2U2vRR9ceZtqZQhDtyw"))
                //val myfile = File(context.getExternalFilesDir(null), "house.jpg")
                //myfile.writeBytes(data3)

                val data3: ByteArray = ipfs.cat(Multihash.fromBase58("QmbChQDjwR8ZTjdXHJMHP9eMiYRRRvSDAgmNNV22HggM7J"))
                Log.i("test123", "data: " + String(data3, Charsets.UTF_8))

                val file = NamedStreamable.ByteArrayWrapper("hello.txt", "test test ajdcbidsc".toByteArray())

                val multihash = ipfs.add(file, false, false).get(0).hash
                Log.i("test123", "multihash: " + multihash.toString())

                val tmp = ByteArray(10240000)
                val file2 = NamedStreamable.ByteArrayWrapper("test.txt", tmp)

                val multihash2 = ipfs.add(file2, false, false).get(0).hash
                Log.i("test123", "multihash2: " + multihash2.toString())

            }
        }
    }
}
