package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository

import androidx.lifecycle.LiveData
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity

internal interface IPFSObjectRepository {
    suspend fun getIPFSObject(hash: String): Either<ErrorEntity, IPFSObject>

    suspend fun getByMetaHash(metaHash: String): Either<ErrorEntity, IPFSObject>

    fun observe(hash: String): LiveData<IPFSObject?>

    fun observeByMetaHash(metaHash: String): LiveData<IPFSObject?>

    suspend fun create(ipfsObject: IPFSObject): Either<ErrorEntity, Unit>

    fun observeAll(): LiveData<List<IPFSObject>>

    suspend fun getAll(): Either<ErrorEntity, List<IPFSObject>>

    suspend fun getRootByType(rootType: IPFSObjectType): Either<ErrorEntity, IPFSObject?>

    suspend fun update(ipfsObject: IPFSObject): Either<ErrorEntity, Unit>

    suspend fun delete(ipfsObject: IPFSObject): Either<ErrorEntity, Unit>

    suspend fun exists(contentHash: String): Either<ErrorEntity, Boolean>

    suspend fun existsByMetaHash(metaHash: String): Either<ErrorEntity, Boolean>

    fun observeNextVersion(metaHash: String): LiveData<IPFSObject?>

    fun observePreviousVersion(previousVersionHash: String): LiveData<IPFSObject?>

    suspend fun getParents(metaHash: String): Either<ErrorEntity, List<IPFSObject>>
}
