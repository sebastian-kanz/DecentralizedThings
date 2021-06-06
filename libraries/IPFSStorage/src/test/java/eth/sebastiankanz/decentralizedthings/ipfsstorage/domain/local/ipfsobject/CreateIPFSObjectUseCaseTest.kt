package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import com.google.gson.Gson
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSMetaObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSObjectRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.extensions.getMetaFileNameFromFileName
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class CreateIPFSObjectUseCaseTest {

    private lateinit var ipfsObjectRepo: IPFSObjectRepository
    private lateinit var ipfsUseCase: IPFSUseCase
    private lateinit var localStorageUseCase: LocalStorageUseCase
    private lateinit var createIPFSObjectUseCase: CreateIPFSObjectUseCase

    private val fileContent = "Test123".toByteArray()
    private val fileName = "fileName"
    private val gson = Gson()

    private val now = 1550160535168L
    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(now), ZoneId.systemDefault())

    private val contentHash = "QmYJpwuVFJFsTff4eAWT4kuu4QYTkkkiyjCigMJCGn7uSE"
    private val metaHash = "QmS5p7sT3BkCK3Z6h4n6shADecydafnGRtucJnv3LstBEF"

    @Before
    fun setUp() {
        mockkStatic(Clock::class)
        every { Clock.systemUTC() } returns fixedClock
        ipfsObjectRepo = mockk()
        ipfsUseCase = mockk()
        localStorageUseCase = mockk()
        createIPFSObjectUseCase = CreateIPFSObjectUseCase(ipfsObjectRepo, ipfsUseCase, localStorageUseCase)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `creating an IPFS object works as expected`() {
        coEvery { ipfsObjectRepo.create(any()) } returns Either.Right(Unit)
        val encryptionBundleContent = EncryptionBundle(123L, contentHash, "keyName1", ByteArray(0))
        coEvery { ipfsUseCase.uploadToIPFS(fileContent, false, fileName) } returns Either.Right(encryptionBundleContent)
        val metaObject = IPFSMetaObject(
            contentHash = contentHash,
            previousVersionHash = null,
            version = (0 + 1),
            name = fileName,
            timestamp = now,
            decryptedSize = fileContent.size.toLong(),
            contentIV = ByteArray(0),
            objects = emptyList(),
        )
        val metaObjectJSON = gson.toJson(metaObject)
        val encryptionBundleMeta = EncryptionBundle(123L, metaHash, "keyName2", ByteArray(0))

        coEvery { ipfsUseCase.uploadToIPFS(metaObjectJSON.toByteArray(), false, fileName.getMetaFileNameFromFileName()) } returns Either.Right(
            encryptionBundleMeta
        )
        every { localStorageUseCase.writeContent(any(), any(), any()) } returns ""
        every { localStorageUseCase.writeMetaData(any(), any(), any()) } returns ""

        runBlockingTest {
            val result = createIPFSObjectUseCase.create(fileContent, fileName, IPFSObjectType.FILE)
            val expected = Either.Right(
                IPFSObject(
                    metaHash = metaHash,
                    contentHash = contentHash,
                    previousVersionHash = null,
                    version = (0 + 1),
                    type = IPFSObjectType.FILE,
                    name = fileName,
                    timestamp = now,
                    decryptedSize = fileContent.size.toLong(),
                    syncState = SyncState.SYNCED,
                    localPath = null,
                    localMetaPath = null,
                    contentIV = ByteArray(0),
                    metaIV = ByteArray(0),
                    objects = emptyList()
                )
            )
            assertEquals(expected, result)
        }
    }

    @Test
    fun `handles error state when localStorageUseCase fails on content data`() {
        every { localStorageUseCase.writeContent(any(), any(), any()) } throws Exception("Something went wrong")

        runBlockingTest {
            val result = createIPFSObjectUseCase.create(fileContent, fileName, IPFSObjectType.FILE)
            val expected = Either.Left(ErrorEntity.UseCaseError.CreateObjectError("Something went wrong"))
            assertEquals(expected, result)
        }
    }

    @Test
    fun `handles error state when localStorageUseCase fails on meta data`() {
        coEvery { ipfsObjectRepo.create(any()) } returns Either.Right(Unit)
        val encryptionBundleContent = EncryptionBundle(123L, contentHash, "keyName1", ByteArray(0))
        coEvery { ipfsUseCase.uploadToIPFS(fileContent, false, fileName) } returns Either.Right(encryptionBundleContent)
        every { localStorageUseCase.writeContent(any(), any(), any()) } returns ""
        every { localStorageUseCase.writeMetaData(any(), any(), any()) } throws Exception("Something went wrong")

        runBlockingTest {
            val result = createIPFSObjectUseCase.create(fileContent, fileName, IPFSObjectType.FILE)
            val expected = Either.Left(ErrorEntity.UseCaseError.CreateObjectError("Something went wrong"))
            assertEquals(expected, result)
        }
    }

    @Test
    fun `handles error state when ipfsUseCase fails on content data`() {
        coEvery { ipfsObjectRepo.create(any()) } returns Either.Right(Unit)
        coEvery {
            ipfsUseCase.uploadToIPFS(
                fileContent,
                false,
                fileName
            )
        } returns Either.Left(ErrorEntity.UseCaseError.UploadToIPFSFailed("Something went wrong"))

        every { localStorageUseCase.writeContent(any(), any(), any()) } returns ""

        runBlockingTest {
            val result = createIPFSObjectUseCase.create(fileContent, fileName, IPFSObjectType.FILE)
            val expected = Either.Left(ErrorEntity.UseCaseError.UploadToIPFSFailed("Something went wrong"))
            assertEquals(expected, result)
        }
    }

    @Test
    fun `handles error state when ipfsUseCase fails on meta data`() {
        coEvery { ipfsObjectRepo.create(any()) } returns Either.Right(Unit)
        val encryptionBundleContent = EncryptionBundle(123L, contentHash, "keyName1", ByteArray(0))
        coEvery { ipfsUseCase.uploadToIPFS(fileContent, false, fileName) } returns Either.Right(encryptionBundleContent)
        val metaObject = IPFSMetaObject(
            contentHash = contentHash,
            previousVersionHash = null,
            version = (0 + 1),
            name = fileName,
            timestamp = now,
            decryptedSize = fileContent.size.toLong(),
            contentIV = ByteArray(0),
            objects = emptyList(),
        )
        val metaObjectJSON = gson.toJson(metaObject)

        coEvery {
            ipfsUseCase.uploadToIPFS(
                metaObjectJSON.toByteArray(),
                false,
                fileName.getMetaFileNameFromFileName()
            )
        } returns Either.Left(ErrorEntity.UseCaseError.UploadToIPFSFailed("Something went wrong"))
        every { localStorageUseCase.writeContent(any(), any(), any()) } returns ""
        every { localStorageUseCase.writeMetaData(any(), any(), any()) } returns ""


        runBlockingTest {
            val result = createIPFSObjectUseCase.create(fileContent, fileName, IPFSObjectType.FILE)
            val expected = Either.Left(ErrorEntity.UseCaseError.UploadToIPFSFailed("Something went wrong"))
            assertEquals(expected, result)
        }
    }

    @Test
    fun `handles error state when ipfsObjectRepo fails`() {
        coEvery { ipfsObjectRepo.create(any()) } returns Either.Left(ErrorEntity.RepoError.IPFSObjectError("Something went wrong."))
        val encryptionBundleContent = EncryptionBundle(123L, contentHash, "keyName1", ByteArray(0))
        coEvery { ipfsUseCase.uploadToIPFS(fileContent, false, fileName) } returns Either.Right(encryptionBundleContent)
        val metaObject = IPFSMetaObject(
            contentHash = contentHash,
            previousVersionHash = null,
            version = (0 + 1),
            name = fileName,
            timestamp = now,
            decryptedSize = fileContent.size.toLong(),
            contentIV = ByteArray(0),
            objects = emptyList(),
        )
        val metaObjectJSON = gson.toJson(metaObject)
        val encryptionBundleMeta = EncryptionBundle(123L, metaHash, "keyName2", ByteArray(0))

        coEvery { ipfsUseCase.uploadToIPFS(metaObjectJSON.toByteArray(), false, fileName.getMetaFileNameFromFileName()) } returns Either.Right(
            encryptionBundleMeta
        )
        every { localStorageUseCase.writeContent(any(), any(), any()) } returns ""
        every { localStorageUseCase.writeMetaData(any(), any(), any()) } returns ""

        runBlockingTest {
            val result = createIPFSObjectUseCase.create(fileContent, fileName, IPFSObjectType.FILE)
            val expected = Either.Left(ErrorEntity.RepoError.IPFSObjectError("Something went wrong."))
            assertEquals(expected, result)
        }
    }
}