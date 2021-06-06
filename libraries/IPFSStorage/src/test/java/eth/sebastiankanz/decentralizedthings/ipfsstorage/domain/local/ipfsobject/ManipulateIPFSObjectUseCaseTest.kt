package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSObjectRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.EncryptionUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ManipulateIPFSObjectUseCaseTest {

    private lateinit var ipfsObjectRepo: IPFSObjectRepository
    private lateinit var ipfsUseCase: IPFSUseCase
    private lateinit var localStorageUseCase: LocalStorageUseCase
    private lateinit var createIPFSObjectUseCase: CreateIPFSObjectUseCase
    private lateinit var encryptionUseCase: EncryptionUseCase
    private lateinit var syncIPFSObjectUseCase: SyncIPFSObjectUseCase
    private lateinit var updateChildObjectsUseCase: UpdateChildObjectsUseCase
    private lateinit var manipulateIPFSObjectUseCase: ManipulateIPFSObjectUseCase

    private val now = 1550160535168L
    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(now), ZoneId.systemDefault())

    private val contentHash = "QmYJpwuVFJFsTff4eAWT4kuu4QYTkkkiyjCigMJCGn7uSE"
    private val metaHash = "QmS5p7sT3BkCK3Z6h4n6shADecydafnGRtucJnv3LstBEF"
    private val updatedContentHash = "QmfCYCZuFHyqWpcGrSpCUZydYiMPRBipLR7NMJv2DjNdA9"
    private val updatedMetaHash = "Qmc5gCcjYypU7y28oCALwfSvxCBskLuPKWpK4qpterKC7z"

    private lateinit var oldObject: IPFSObject
    private lateinit var updatedObject: IPFSObject
    private lateinit var renamedObject: IPFSObject

    private lateinit var newContent: ByteArray
    private lateinit var fileContent: ByteArray
    private lateinit var newFileName: String

    @Before
    fun setUp() {
        mockkStatic(Clock::class)
        every { Clock.systemUTC() } returns fixedClock
        ipfsObjectRepo = mockk()
        ipfsUseCase = mockk()
        localStorageUseCase = mockk()
        createIPFSObjectUseCase = mockk()
        encryptionUseCase = mockk()
        syncIPFSObjectUseCase = mockk()
        updateChildObjectsUseCase = mockk()
        manipulateIPFSObjectUseCase = spyk(
            ManipulateIPFSObjectUseCase(
                ipfsObjectRepo,
                ipfsUseCase,
                localStorageUseCase,
                createIPFSObjectUseCase,
                encryptionUseCase,
                syncIPFSObjectUseCase,
                updateChildObjectsUseCase
            ),
            recordPrivateCalls = true,
        )
        oldObject = IPFSObject(
            contentHash = contentHash,
            metaHash = metaHash,
            previousVersionHash = null,
            version = 1,
            type = IPFSObjectType.FILE,
            name = "name",
            timestamp = now,
            decryptedSize = 0L,
            syncState = SyncState.SYNCED,
            localPath = null,
            localMetaPath = null,
            contentIV = ByteArray(0),
            metaIV = ByteArray(0),
            objects = emptyList()
        )
        newContent = "New Content".toByteArray()
        fileContent = "Content".toByteArray()
        updatedObject = IPFSObject(
            contentHash = updatedContentHash,
            metaHash = updatedMetaHash,
            previousVersionHash = metaHash,
            version = 2,
            type = IPFSObjectType.FILE,
            name = "name",
            timestamp = now,
            decryptedSize = newContent.size.toLong(),
            syncState = SyncState.SYNCED,
            localPath = null,
            localMetaPath = null,
            contentIV = ByteArray(0),
            metaIV = ByteArray(0),
            objects = emptyList()
        )
        newFileName = "new name"
        renamedObject = IPFSObject(
            contentHash = updatedContentHash,
            metaHash = updatedMetaHash,
            previousVersionHash = metaHash,
            version = 2,
            type = IPFSObjectType.FILE,
            name = newFileName,
            timestamp = now,
            decryptedSize = fileContent.size.toLong(),
            syncState = SyncState.SYNCED,
            localPath = null,
            localMetaPath = null,
            contentIV = ByteArray(0),
            metaIV = ByteArray(0),
            objects = emptyList()
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `updating content of an IPFS object works as expected`() {
        coEvery {
            createIPFSObjectUseCase.create(
                newContent,
                oldObject.name,
                oldObject.type,
                oldObject.localPath,
                any(),
                oldObject.previousVersionHash,
                oldObject.version,
                any()
            )
        } returns Either.Right(updatedObject)
        coEvery { manipulateIPFSObjectUseCase.replaceIPFSObjectForAllParents(oldObject, updatedObject, any()) } returns Either.Right(Unit)
        coEvery { manipulateIPFSObjectUseCase.deleteIPFSObject(any(), any()) } returns Either.Right(Unit)

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.updateIPFSObjectContent(oldObject, newContent)
            assertEquals(Either.Right(updatedObject), result)
        }
    }

    @Test
    fun `updating content of an IPFS object works as expected when updating parents fails`() {
        coEvery {
            createIPFSObjectUseCase.create(
                newContent,
                oldObject.name,
                oldObject.type,
                oldObject.localPath,
                any(),
                oldObject.previousVersionHash,
                oldObject.version,
                any()
            )
        } returns Either.Right(updatedObject)

        coEvery { manipulateIPFSObjectUseCase.replaceIPFSObjectForAllParents(oldObject, updatedObject, any()) } returns Either.Left(
            ErrorEntity.UseCaseError.ManipulateObjectError("Something went wrong")
        )
        coEvery { manipulateIPFSObjectUseCase.deleteIPFSObject(any(), any()) } returns Either.Right(Unit)

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.updateIPFSObjectContent(oldObject, newContent)
            assertEquals(Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Something went wrong")), result)
        }
    }

    @Test
    fun `updating content of an IPFS object works as expected when deleting object fails`() {
        coEvery {
            createIPFSObjectUseCase.create(
                newContent,
                oldObject.name,
                oldObject.type,
                oldObject.localPath,
                any(),
                oldObject.previousVersionHash,
                oldObject.version,
                any()
            )
        } returns Either.Right(updatedObject)

        coEvery { manipulateIPFSObjectUseCase.replaceIPFSObjectForAllParents(oldObject, updatedObject, any()) } returns Either.Right(Unit)
        coEvery { manipulateIPFSObjectUseCase.deleteIPFSObject(any(), any()) } returns Either.Left(
            ErrorEntity.UseCaseError.ManipulateObjectError("Something went wrong")
        )

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.updateIPFSObjectContent(oldObject, newContent)
            assertEquals(Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Something went wrong")), result)
        }
    }

    @Test
    fun `renaming of an IPFS object works as expected`() {

        coEvery {
            createIPFSObjectUseCase.create(
                fileContent,
                newFileName,
                oldObject.type,
                oldObject.localPath,
                any(),
                oldObject.previousVersionHash,
                oldObject.version,
                any()
            )
        } returns Either.Right(renamedObject)

        coEvery { syncIPFSObjectUseCase.syncObjectFromIPFS(oldObject) } returns Either.Right(oldObject)
        every { localStorageUseCase.getObjectContent(oldObject) } returns fileContent
        coEvery { manipulateIPFSObjectUseCase.replaceIPFSObjectForAllParents(oldObject, renamedObject, any()) } returns Either.Right(Unit)
        coEvery { manipulateIPFSObjectUseCase.deleteIPFSObject(any(), any()) } returns Either.Right(Unit)

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.renameIPFSObject(oldObject, newFileName)
            assertEquals(Either.Right(renamedObject), result)
        }
    }

    @Test
    fun `renaming of an IPFS object works as expected when syncing object fails`() {
        coEvery {
            createIPFSObjectUseCase.create(
                fileContent,
                newFileName,
                oldObject.type,
                oldObject.localPath,
                any(),
                oldObject.previousVersionHash,
                oldObject.version,
                any()
            )
        } returns Either.Right(renamedObject)

        coEvery { syncIPFSObjectUseCase.syncObjectFromIPFS(oldObject) } returns Either.Left(ErrorEntity.UseCaseError.SyncObjectError("Something went wrong."))

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.renameIPFSObject(oldObject, newFileName)
            assertEquals(Either.Left(ErrorEntity.UseCaseError.SyncObjectError("Something went wrong.")), result)
        }
    }

    @Test
    fun `renaming of an IPFS object works as expected when reading object content fails`() {
        coEvery {
            createIPFSObjectUseCase.create(
                fileContent,
                newFileName,
                oldObject.type,
                oldObject.localPath,
                any(),
                oldObject.previousVersionHash,
                oldObject.version,
                any()
            )
        } returns Either.Right(renamedObject)

        coEvery { syncIPFSObjectUseCase.syncObjectFromIPFS(oldObject) } returns Either.Right(oldObject)
        every { localStorageUseCase.getObjectContent(oldObject) } throws Exception("Something went wrong")

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.renameIPFSObject(oldObject, newFileName)
            assertEquals(Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Something went wrong")), result)
        }
    }

    @Test
    fun `renaming of an IPFS object works as expected when creating object fails`() {
        coEvery {
            createIPFSObjectUseCase.create(
                fileContent,
                newFileName,
                oldObject.type,
                oldObject.localPath,
                any(),
                oldObject.previousVersionHash,
                oldObject.version,
                any()
            )
        } returns Either.Left(ErrorEntity.UseCaseError.CreateObjectError("Something went wrong"))

        coEvery { syncIPFSObjectUseCase.syncObjectFromIPFS(oldObject) } returns Either.Right(oldObject)
        every { localStorageUseCase.getObjectContent(oldObject) } returns fileContent

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.renameIPFSObject(oldObject, newFileName)
            assertEquals(Either.Left(ErrorEntity.UseCaseError.CreateObjectError("Something went wrong")), result)
        }
    }

    @Test
    fun `renaming of an IPFS object works as expected when updating parents fails`() {
        coEvery {
            createIPFSObjectUseCase.create(
                fileContent,
                newFileName,
                oldObject.type,
                oldObject.localPath,
                any(),
                oldObject.previousVersionHash,
                oldObject.version,
                any()
            )
        } returns Either.Right(renamedObject)

        coEvery { syncIPFSObjectUseCase.syncObjectFromIPFS(oldObject) } returns Either.Right(oldObject)
        every { localStorageUseCase.getObjectContent(oldObject) } returns fileContent
        coEvery {
            manipulateIPFSObjectUseCase.replaceIPFSObjectForAllParents(
                oldObject,
                renamedObject,
                any()
            )
        } returns Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Something went wrong"))

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.renameIPFSObject(oldObject, newFileName)
            assertEquals(Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Something went wrong")), result)
        }
    }

    @Test
    fun `renaming of an IPFS object works as expected when deleting object fails`() {
        coEvery {
            createIPFSObjectUseCase.create(
                fileContent,
                newFileName,
                oldObject.type,
                oldObject.localPath,
                any(),
                oldObject.previousVersionHash,
                oldObject.version,
                any()
            )
        } returns Either.Right(renamedObject)

        coEvery { syncIPFSObjectUseCase.syncObjectFromIPFS(oldObject) } returns Either.Right(oldObject)
        every { localStorageUseCase.getObjectContent(oldObject) } returns fileContent
        coEvery {
            manipulateIPFSObjectUseCase.replaceIPFSObjectForAllParents(
                oldObject,
                renamedObject,
                any()
            )
        } returns Either.Right(Unit)
        coEvery {
            manipulateIPFSObjectUseCase.deleteIPFSObject(
                any(),
                any()
            )
        } returns Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Something went wrong"))

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.renameIPFSObject(oldObject, newFileName)
            assertEquals(Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Something went wrong")), result)
        }
    }

    @Test
    fun `deleting IPFS object returns error entity when object exists only locally and force deletion is false`() {
        val objectToDelete = IPFSObject(
            syncState = SyncState.UNSYNCED_ONLY_LOCAL,
        )
        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.deleteIPFSObject(objectToDelete, true, false)
            assertEquals(
                Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Can't delete ipfsObject locally as it is not synced remotely and therefore data would be lost. Forcing is disabled.")),
                result
            )
        }
    }

    @Test
    fun `deleting IPFS object works as expected when deletion is only locally`() {
        val objectToDelete = IPFSObject(
            syncState = SyncState.SYNCED,
        )
        val updatedObject = objectToDelete.copy(syncState = SyncState.UNSYNCED_ONLY_REMOTE, localPath = null)
        coEvery { ipfsObjectRepo.update(updatedObject) } returns Either.Right(Unit)
        every { localStorageUseCase.deleteContentObject(objectToDelete) } returns Unit
        every { localStorageUseCase.deleteMetaObject(objectToDelete) } returns Unit
        coEvery { manipulateIPFSObjectUseCase.deleteIPFSObjectFromAllParents(objectToDelete, any()) } returns Either.Right(Unit)

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.deleteIPFSObject(objectToDelete, true, false)
            assertEquals(
                Either.Right(Unit),
                result
            )
        }
    }

    @Test
    fun `deleting IPFS object works as expected when deletion is only locally and force deletion is true`() {
        val objectToDelete = IPFSObject(
            syncState = SyncState.UNSYNCED_ONLY_LOCAL,
        )
        val updatedObject = objectToDelete.copy(syncState = SyncState.UNSYNCED_ONLY_REMOTE, localPath = null)
        coEvery { ipfsObjectRepo.update(updatedObject) } returns Either.Right(Unit)
        every { localStorageUseCase.deleteContentObject(objectToDelete) } returns Unit
        every { localStorageUseCase.deleteMetaObject(objectToDelete) } returns Unit
        coEvery { manipulateIPFSObjectUseCase.deleteIPFSObjectFromAllParents(objectToDelete, any()) } returns Either.Right(Unit)

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.deleteIPFSObject(objectToDelete, true, true)
            assertEquals(
                Either.Right(Unit),
                result
            )
        }
    }

    @Test
    fun `deleting IPFS object works as expected when deletion is locally and remotely and object is only saved locally`() {
        val objectToDelete = IPFSObject(
            syncState = SyncState.UNSYNCED_ONLY_LOCAL,
        )
        every { encryptionUseCase.deleteKeysForObject(objectToDelete) } returns Unit
        coEvery { ipfsObjectRepo.delete(objectToDelete) } returns Either.Right(Unit)
        every { localStorageUseCase.deleteContentObject(objectToDelete) } returns Unit
        every { localStorageUseCase.deleteMetaObject(objectToDelete) } returns Unit
        coEvery { manipulateIPFSObjectUseCase.deleteIPFSObjectFromAllParents(objectToDelete, any()) } returns Either.Right(Unit)

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.deleteIPFSObject(objectToDelete, false, false)
            assertEquals(
                Either.Right(Unit),
                result
            )
        }
    }

    @Test
    fun `deleting IPFS object works as expected when deletion is locally and remotely`() {
        val objectToDelete = IPFSObject(
            syncState = SyncState.SYNCED,
        )
        every { encryptionUseCase.deleteKeysForObject(objectToDelete) } returns Unit
        coEvery { ipfsObjectRepo.delete(objectToDelete) } returns Either.Right(Unit)
        every { localStorageUseCase.deleteContentObject(objectToDelete) } returns Unit
        every { localStorageUseCase.deleteMetaObject(objectToDelete) } returns Unit
        coEvery { manipulateIPFSObjectUseCase.deleteIPFSObjectFromAllParents(objectToDelete, any()) } returns Either.Right(Unit)
        coEvery { ipfsUseCase.deleteFromIPFS(objectToDelete.contentHash) } returns Either.Right(true)
        coEvery { ipfsUseCase.deleteFromIPFS(objectToDelete.metaHash) } returns Either.Right(true)

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.deleteIPFSObject(objectToDelete, false, false)
            assertEquals(
                Either.Right(Unit),
                result
            )
        }
    }

    @Test
    fun `deleting IPFS object from all parents works as expected`() {
        val objectToDelete = IPFSObject(metaHash = "111", metaIV = "aaa".toByteArray())
        val parents = listOf(
            IPFSObject(
                metaHash = "222",
                objects = listOf(Pair("111", "aaa".toByteArray().decodeToString()))
            ),
            IPFSObject(
                metaHash = "333",
                objects = listOf(Pair("111", "aaa".toByteArray().decodeToString()))
            )
        )
        coEvery { ipfsObjectRepo.getParents(objectToDelete.metaHash) } returns Either.Right(parents)
        coEvery { ipfsObjectRepo.getParents("222") } returns Either.Right(emptyList())
        coEvery { ipfsObjectRepo.getParents("333") } returns Either.Right(emptyList())

        coEvery { updateChildObjectsUseCase.removeObject(parents[0], objectToDelete, any()) } returns Either.Right(
            IPFSObject(
                metaHash = "222",
                objects = emptyList()
            )
        )
        coEvery { updateChildObjectsUseCase.removeObject(parents[1], objectToDelete, any()) } returns Either.Right(
            IPFSObject(
                metaHash = "333",
                objects = emptyList()
            )
        )

        coEvery {
            manipulateIPFSObjectUseCase.deleteIPFSObjectFromAllParents(
                IPFSObject(
                    metaHash = "222",
                    objects = emptyList()
                ), any()
            )
        } returns Either.Right(Unit)
        coEvery {
            manipulateIPFSObjectUseCase.deleteIPFSObjectFromAllParents(
                IPFSObject(
                    metaHash = "333",
                    objects = emptyList()
                ), any()
            )
        } returns Either.Right(Unit)

        runBlockingTest {
            val result = manipulateIPFSObjectUseCase.deleteIPFSObjectFromAllParents(objectToDelete)
            assertEquals(
                Either.Right(Unit),
                result
            )
        }
    }
}