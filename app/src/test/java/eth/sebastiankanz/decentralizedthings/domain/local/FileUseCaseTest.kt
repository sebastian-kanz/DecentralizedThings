package eth.sebastiankanz.decentralizedthings.domain.local

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.test.KoinTest

@ExperimentalCoroutinesApi
class FileUseCaseTest : KoinTest {

//    private val fileRepo = mockk<FileRepository> {
//        every { observeByMetaHash(any()) } returns MutableLiveData<File?>(File())
//        every { observeNextVersion(any()) } returns MutableLiveData<File?>(File())
//        every { observePreviousVersion(any()) } returns MutableLiveData<File?>(File())
//    }
//    private val ipfsUseCase = mockk<IPFSUseCase> {
//        every { uploadToIPFS(any(), any()) } returns MutableLiveData<Pair<String, ByteArray>>(Pair("hash", EncryptionBundle().encryptionKey))
//        every { downloadFromIPFS(any(), any()) } returns MutableLiveData<ByteArray>("fileContent".toByteArray())
//    }
//    private val encryptionKeyUseCase = mockk<EncryptionUseCase> {
//        every { getDecryptionKey(any()) } returns MutableLiveData<EncryptionBundle>(EncryptionBundle())
//    }
//    private val localStorageUseCase = mockk<LocalStorageUseCase> {
//
//    }
//    private val sharableFileUseCase = mockk<ShareFileUseCase> {
//
//    }
//    private val versionableFileUseCase = mockk<VersionableFileUseCase> {
//
//    }
//
//    @get:Rule
//    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()
//
//    @get:Rule
//    val testCoroutineRule = TestCoroutineRule()
//
//    @Before
//    fun setUp() {
//        startKoin {
//            modules(
//                listOf(
//                    androidModule,
//                    applicationModule,
//                    databaseModule,
//                    ipfsModule,
//                    repositoryModule,
//                    useCaseModule,
//                    viewModelModule
//                )
//            )
//        }
//    }
//
//    @After
//    fun tearDown() {
//        stopKoin()
//    }
//
//    @Test
//    fun getGson() {
//    }
//
//    @Test
//    fun observeAllFiles() {
//    }
//
//    @Test
//    fun observeFile() {
//    }
//
//    @Test
//    fun createFile() {
//        runBlocking {
//            val usecase = GetFileUseCase(fileRepo, ipfsUseCase, encryptionKeyUseCase, localStorageUseCase, sharableFileUseCase, versionableFileUseCase)
////            usecase.create()
//        }
//    }
//
//    @Test
//    fun getFileFromIPFS() {
//    }
//
//    @Test
//    fun getContentFromIPFS() {
//    }
//
//    @Test
//    fun updateFileContent() {
//    }
//
//    @Test
//    fun updateFileName() {
//    }
//
//    @Test
//    fun updateFileContentAndName() {
//    }
//
//    @Test
//    fun deleteLocalContent() {
//    }
//
//    @Test
//    fun deleteFile() {
//    }
//
//    @Test
//    fun hasPreviousVersion() {
//    }
//
//    @Test
//    fun observeHasNextVersion() {
//    }
//
//    @Test
//    fun observePreviousFileVersion() {
//    }
//
//    @Test
//    fun observeNextFileVersion() {
//    }
//
//    @Test
//    fun isShared() {
//    }
//
//    @Test
//    fun getSharedWithPubKeys() {
//    }
}