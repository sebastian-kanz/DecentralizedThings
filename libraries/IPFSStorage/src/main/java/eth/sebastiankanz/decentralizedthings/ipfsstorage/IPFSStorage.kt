package eth.sebastiankanz.decentralizedthings.ipfsstorage

import android.graphics.Bitmap
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectExportable
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.di.modules.databaseModule
import eth.sebastiankanz.decentralizedthings.ipfsstorage.di.modules.networkModule
import eth.sebastiankanz.decentralizedthings.ipfsstorage.di.modules.repositoryModule
import eth.sebastiankanz.decentralizedthings.ipfsstorage.di.modules.useCaseModule
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.CreateIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.GetIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.ImportIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.ManipulateIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.ShareIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.SyncIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import eth.sebastiankanz.decentralizedthings.qrcodemanager.QRCodeManager
import kotlinx.serialization.json.Json
import org.koin.core.context.loadKoinModules
import org.koin.java.KoinJavaComponent.inject

class IPFSStorage private constructor() {
    private val createIPFSObjectUseCase: CreateIPFSObjectUseCase by inject(CreateIPFSObjectUseCase::class.java)
    private val getIPFSObjectUseCase: GetIPFSObjectUseCase by inject(GetIPFSObjectUseCase::class.java)
    private val importIPFSObjectUseCase: ImportIPFSObjectUseCase by inject(ImportIPFSObjectUseCase::class.java)
    private val manipulateIPFSObjectUseCase: ManipulateIPFSObjectUseCase by inject(ManipulateIPFSObjectUseCase::class.java)
    private val shareIPFSObjectUseCase: ShareIPFSObjectUseCase by inject(ShareIPFSObjectUseCase::class.java)
    private val syncIPFSObjectUseCase: SyncIPFSObjectUseCase by inject(SyncIPFSObjectUseCase::class.java)

    fun observeAll() = getIPFSObjectUseCase.observeAll()
    fun observeAllLatest() = getIPFSObjectUseCase.observeAllLatest()

    fun observeAllByType(type: IPFSObjectType) = getIPFSObjectUseCase.observeAllByType(type)
    fun observeAllLatestByType(type: IPFSObjectType) = getIPFSObjectUseCase.observeAllLatestByType(type)
    suspend fun getByMetaHash(metaHash: String) = getIPFSObjectUseCase.getByMetaHash(metaHash)

    suspend fun createIPFSObject(
        data: ByteArray,
        name: String,
        type: IPFSObjectType,
        path: String? = null,
        override: Boolean = false,
        previousVersionHash: String? = null,
        previousVersionNumber: Int = 0,
        onlyLocally: Boolean = false,
        objects: List<Pair<String, String>> = emptyList()
    ) = createIPFSObjectUseCase.create(data, name, type, path, override, previousVersionHash, previousVersionNumber, onlyLocally, objects)

    suspend fun deleteIPFSObject(
        ipfsObject: IPFSObject,
        onlyLocally: Boolean = false
    ) = manipulateIPFSObjectUseCase.deleteIPFSObject(ipfsObject, onlyLocally)

    suspend fun renameIPFSObject(
        ipfsObject: IPFSObject,
        newName: String,
        onlyLocally: Boolean = false,
        override: Boolean = false
    ) = manipulateIPFSObjectUseCase.renameIPFSObject(
        ipfsObject,
        newName,
        onlyLocally,
        override,
    )

    suspend fun syncIPFSObjectToIPFS(
        ipfsObject: IPFSObject
    ) = syncIPFSObjectUseCase.syncObjectToIPFS(ipfsObject)

    suspend fun syncIPFSObjectFromIPFS(
        ipfsObject: IPFSObject
    ) = syncIPFSObjectUseCase.syncObjectFromIPFS(ipfsObject)

    suspend fun exportIPFSObjectQRCode(
        ipfsObject: IPFSObject
    ): Either<ErrorEntity, Bitmap> {
        val qrCodeManager = QRCodeManager.newInstance()
        val exportableObject = shareIPFSObjectUseCase.generateExportableObject(ipfsObject)
        val json = Json.encodeToString(IPFSObjectExportable.serializer(), exportableObject)
        return Either.Right(qrCodeManager.generateQRCode(json))
    }

    companion object {
        fun newInstance(): IPFSStorage {
            loadKoinModules(
                listOf(
                    databaseModule,
                    networkModule,
                    repositoryModule,
                    useCaseModule
                )
            )
            return IPFSStorage()
        }
    }
}