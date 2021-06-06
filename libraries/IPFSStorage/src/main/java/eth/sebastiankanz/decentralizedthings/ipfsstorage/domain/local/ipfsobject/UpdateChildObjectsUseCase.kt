package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import java.util.logging.Logger

internal class UpdateChildObjectsUseCase(
    private val localStorageUseCase: LocalStorageUseCase,
    private val createIPFSObjectUseCase: CreateIPFSObjectUseCase,
) {
    companion object {
        private val LOGGER = Logger.getLogger("UpdateChildObjectsUseCase")
    }

    suspend fun updateObjects(
        ipfsObject: IPFSObject,
        objectsToAdd: List<IPFSObject>,
        objectsToRemove: List<IPFSObject>,
        onlyLocally: Boolean
    ): Either<ErrorEntity, IPFSObject> {
        LOGGER.info("Update objects.")
        val toAdd = objectsToAdd.map { Pair(it.metaHash, it.metaIV.decodeToString()) }
        val toRemove = objectsToRemove.map { Pair(it.metaHash, it.metaIV.decodeToString()) }
        val newObjects = ipfsObject.objects.toMutableList()
        newObjects.removeAll(toRemove)
        newObjects.addAll(toAdd)
        return try {
            if (localStorageUseCase.getContentFile(ipfsObject).isDirectory) {
                return createIPFSObjectUseCase.create(
                    ByteArray(0),
                    ipfsObject.name,
                    ipfsObject.type,
                    ipfsObject.localPath,
                    true,
                    ipfsObject.metaHash,
                    ipfsObject.version,
                    onlyLocally,
                    newObjects
                )
            } else {
                Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError("Can not remove objects from non-directory"))
            }
        } catch (e: Exception) {
            Either.Left(ErrorEntity.UseCaseError.ManipulateObjectError(e.message))
        }
    }

    suspend fun addObjects(
        ipfsObject: IPFSObject,
        objectsToAdd: List<IPFSObject>,
        onlyLocally: Boolean
    ) = updateObjects(ipfsObject, objectsToAdd, emptyList(), onlyLocally)

    suspend fun addObject(
        ipfsObject: IPFSObject,
        objectToAdd: IPFSObject,
        onlyLocally: Boolean
    ) = addObjects(ipfsObject, listOf(objectToAdd), onlyLocally)

    suspend fun removeObjects(
        ipfsObject: IPFSObject,
        objectsToRemove: List<IPFSObject>,
        onlyLocally: Boolean
    ) = updateObjects(ipfsObject, emptyList(), objectsToRemove, onlyLocally)

    suspend fun removeObject(
        ipfsObject: IPFSObject,
        objectToRemove: IPFSObject,
        onlyLocally: Boolean
    ) = removeObjects(ipfsObject, listOf(objectToRemove), onlyLocally)
}
