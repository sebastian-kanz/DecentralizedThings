package eth.sebastiankanz.decentralizedthings.data.repository

import androidx.lifecycle.MutableLiveData
import eth.sebastiankanz.decentralizedthings.helper.BaseAndroidTest
import eth.sebastiankanz.decentralizedthings.helper.testObserver
import io.ipfs.multihash.Multihash
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class IPFSRepositoryAndroidTest : BaseAndroidTest() {

    private val hash = "QmczcYpf51WsjeSUsoMmNXKcUtk2U2vRR9ceZtqZQhDtyw"
    private val data = "test123".toByteArray()

    private val ipfsRepo = mockk<IPFSRepository> {
        every { upload(any(), true) } returns MutableLiveData(Multihash.fromBase58(hash).toString())
        every { download(any()) } returns MutableLiveData(data)
    }

    @Test
    fun test1() {
        runBlocking {
            val testObserver = ipfsRepo.upload("test123".toByteArray(), true).testObserver()
            testObserver.assertLastValue(hash)
            val testObserver2 = ipfsRepo.download(hash).testObserver()
            testObserver2.assertLastValue(data)
        }
    }
}