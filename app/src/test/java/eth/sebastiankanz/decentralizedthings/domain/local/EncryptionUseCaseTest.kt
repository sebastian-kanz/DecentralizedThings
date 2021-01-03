package eth.sebastiankanz.decentralizedthings.domain.local

import org.koin.test.KoinTest

class EncryptionUseCaseTest : KoinTest {

//    private val hash = "hash123"
//    private val invalidHash = ""
//    private val encryptionKey = EncryptionBundle(id = 123, ipfsHash = hash, "test123".toByteArray())
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
//    fun testCreateNewEncryptionKeyIV() {
//        val encryptionKeyRepo = mockk<EncryptionBundleRepository>() {
//            every { insertEncryptionKey(any()) } returns MutableLiveData<Long>().apply { postValue(Random.nextLong()) }
//        }
//        runBlocking {
//            val encryptionKeyUseCase = EncryptionUseCase(encryptionKeyRepo)
//            val result = encryptionKeyUseCase.createNewEncryptionKeyIV().getOrAwaitValue()
//
//            val slot = slot<EncryptionBundle>()
//            verify(exactly = 1) { encryptionKeyRepo.insertEncryptionKey(capture(slot)) }
//            confirmVerified(encryptionKeyRepo)
//
//            assertEquals(slot.captured.encryptionKey, result.first.encryptionKey)
//        }
//    }
//
//    @Test
//    fun testGetInvalidHash() {
//        val encryptionKeyRepo = mockk<EncryptionBundleRepository>() {
//            coEvery { getKey(invalidHash) } returns null
//        }
//        val emptyEncryptionKey = EncryptionBundle()
//        runBlocking {
//            val encryptionKeyUseCase = EncryptionUseCase(encryptionKeyRepo)
//            val result = encryptionKeyUseCase.getDecryptionKey(invalidHash).getOrAwaitValue()
//            assertEquals(emptyEncryptionKey.id, result.id)
//            assert(emptyEncryptionKey.encryptionKey.contentEquals(result.encryptionKey))
//            assertEquals(emptyEncryptionKey.ipfsHash, result.ipfsHash)
//        }
//    }
//
//    @Test
//    fun testGetValidHash() {
//        val encryptionKeyRepo = mockk<EncryptionBundleRepository>() {
//            coEvery { getKey(hash) } returns encryptionKey
//        }
//        runBlocking {
//            val encryptionKeyUseCase = EncryptionUseCase(encryptionKeyRepo)
//            val result = encryptionKeyUseCase.getDecryptionKey(hash).getOrAwaitValue()
//            assertEquals(encryptionKey.id, result.id)
//            assert(encryptionKey.encryptionKey.contentEquals(result.encryptionKey))
//            assertEquals(encryptionKey.ipfsHash, result.ipfsHash)
//        }
//    }
//
//    @Test
//    fun checkEncryptionDecryption() {
//        val hash = "Qmc5gCcjYypU7y28oCALwfSvxCBskLuPKWpK4qpterKC7z"
//        val iv = ByteArray(16)
//        SecureRandom().nextBytes(iv)
//        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")
//        keyGenerator.init(256)
//        val secretKey: SecretKey = keyGenerator.generateKey()
//        val encryptionKey = EncryptionBundle(encryptionKey = secretKey.encoded)
//
//        val encrypted = hash.toByteArray().encrypt(encryptionKey, iv)
//        val decrypted = encrypted.decrypt(encryptionKey, iv)
//        assert(hash.toByteArray().contentEquals(decrypted))
//        assertEquals(hash, String(decrypted))
//    }
}