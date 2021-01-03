package eth.sebastiankanz.decentralizedthings.domain.local

import org.koin.test.KoinTest

class IPFSUseCaseTest : KoinTest {

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
//    private fun generateEncryptionKeyBundle(): Pair<EncryptionBundle, ByteArray> {
//        val iv = ByteArray(16)
//        SecureRandom().nextBytes(iv)
//        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")
//        keyGenerator.init(256)
//        val secretKey: SecretKey = keyGenerator.generateKey()
//        val encryptionKey = EncryptionBundle(encryptionKey = secretKey.encoded)
//        return Pair(encryptionKey, iv)
//    }
//
//    @Test
//    fun testUploadingToIPFS_HashChanges() {
//        val data = "Hello World!\r\n".toByteArray()
//        val ipfsRepo = get<IPFSRepository>()
//
//        val encryptionKeyUseCase = mockk<EncryptionUseCase> {
//            every { createNewEncryptionKeyIV() } returns MutableLiveData<Pair<EncryptionBundle, ByteArray>>().apply {
//                postValue(generateEncryptionKeyBundle())
//            } andThen MutableLiveData<Pair<EncryptionBundle, ByteArray>>().apply {
//                postValue(generateEncryptionKeyBundle())
//            }
//            every { updateExistingEncryptionBundle(any(), any()) } just runs
//        }
//        val ipfsUseCase = IPFSUseCase(ipfsRepo, encryptionKeyUseCase)
//
//        val result = runBlocking {
//            ipfsUseCase.uploadToIPFS(data, true).getOrAwaitValue(10)
//        }
//
//        val result2 = runBlocking {
//            ipfsUseCase.uploadToIPFS(data, true).getOrAwaitValue(10)
//        }
//
//        assert(result.first != result2.first)
//    }
//
//    @Test
//    fun testDownloadFromIPFS() {
//        val hash = "Qmc5gCcjYypU7y28oCALwfSvxCBskLuPKWpK4qpterKC7z"
//        val data = "Hello World!\r\n".toByteArray()
//        val encryptionKeyUseCase = mockk<EncryptionUseCase>()
//        val ipfsRepo = get<IPFSRepository>()
//        val ipfsUseCase = IPFSUseCase(ipfsRepo, encryptionKeyUseCase)
//
//        runBlocking {
//            val check = ipfsUseCase.downloadFromIPFS(hash).getOrAwaitValue(10)
//            assertEquals(String(data), String(check))
//        }
//    }
}