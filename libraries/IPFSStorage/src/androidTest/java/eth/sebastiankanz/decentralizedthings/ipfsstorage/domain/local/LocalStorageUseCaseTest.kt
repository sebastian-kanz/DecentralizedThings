package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.ipfsstorage.extensions.getMetaFileNameFromFileName
import org.junit.After
import org.junit.Before
import org.junit.Test

class LocalStorageUseCaseTest {

    private lateinit var context: Context
    private lateinit var localStorageUseCase: LocalStorageUseCase
    private lateinit var ipfsObject: IPFSObject

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        localStorageUseCase = LocalStorageUseCase(context)
        val fileContent = "content".toByteArray()
        val metaContent = "meta".toByteArray()
        ipfsObject = IPFSObject(
            contentHash = "contentHash",
            metaHash = "metaHash",
            previousVersionHash = null,
            version = 1,
            type = IPFSObjectType.FILE,
            name = "name",
            timestamp = 1550160535168L,
            decryptedSize = fileContent.size.toLong(),
            syncState = SyncState.SYNCED,
            localPath = null,
            localMetaPath = null,
            contentIV = ByteArray(0),
            metaIV = ByteArray(0),
            objects = emptyList()
        )
        java.io.File(context.getExternalFilesDir(null), ipfsObject.name).writeBytes(fileContent)
        java.io.File(context.getExternalFilesDir(null), ipfsObject.name.getMetaFileNameFromFileName()).writeBytes(metaContent)
    }

    @After
    fun tearDown() {
        java.io.File(context.getExternalFilesDir(null), ipfsObject.name).delete()
        java.io.File(context.getExternalFilesDir(null), ipfsObject.name.getMetaFileNameFromFileName()).delete()
    }

    @Test
    fun gettingLocalContentFile() {
        val result = localStorageUseCase.getContentFile(ipfsObject)
        assert(result.isFile)
        assert(result.exists())
        assert(result.readBytes().decodeToString() == "content")
    }
}