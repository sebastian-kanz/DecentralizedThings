package eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSObjectRepository
import java.util.logging.Logger

/**
 * Use case for handling objects locally and remotely on ipfs.
 * @author Sebastian Kanz
 */
internal class GetIPFSObjectUseCase(
    private val ipfsObjectRepo: IPFSObjectRepository
) {

    companion object {
        private val LOGGER = Logger.getLogger("GetIPFSObjectUseCase")
    }

    fun observeAll() = ipfsObjectRepo.observeAll()

    fun observeAllLatest(): LiveData<List<IPFSObject>> {
        val mediatorLiveData: MediatorLiveData<List<IPFSObject>> = MediatorLiveData<List<IPFSObject>>()
        mediatorLiveData.addSource(ipfsObjectRepo.observeAll()) { allObjects ->
            val latestVersionObjects = allObjects.filter { outer -> allObjects.none { inner -> inner.previousVersionHash == outer.metaHash } }
            mediatorLiveData.postValue(latestVersionObjects)
        }
        return mediatorLiveData
    }

    fun observeAllByType(type: IPFSObjectType) : LiveData<List<IPFSObject>> {
        val mediatorLiveData: MediatorLiveData<List<IPFSObject>> = MediatorLiveData<List<IPFSObject>>()
        mediatorLiveData.addSource(ipfsObjectRepo.observeAll()) { allObjects ->
            val latestVersionObjects = allObjects.filter { it.type == type }
            mediatorLiveData.postValue(latestVersionObjects)
        }
        return mediatorLiveData
    }

    fun observeAllLatestByType(type: IPFSObjectType): LiveData<List<IPFSObject>> {
        val mediatorLiveData: MediatorLiveData<List<IPFSObject>> = MediatorLiveData<List<IPFSObject>>()
        mediatorLiveData.addSource(ipfsObjectRepo.observeAll()) { allObjects ->
            val latestVersionObjects = allObjects.filter { it.type == type }.filter { outer -> allObjects.none { inner -> inner.previousVersionHash == outer.metaHash } }
            mediatorLiveData.postValue(latestVersionObjects)
        }
        return mediatorLiveData
    }

    suspend fun getByMetaHash(metaHash: String) =  ipfsObjectRepo.getByMetaHash(metaHash)

//    fun hasPreviousVersion(file: File): Boolean {
//        if (file.previousVersionHash != null) {
//            if (file.previousVersionHash.isNotBlank() && file.previousVersionHash.isNotEmpty()) {
//                return true
//            }
//        }
//        return false
//    }
//
//    fun observeHasNextVersion(file: File): LiveData<Boolean> {
//        val mediatorLiveData: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>()
//        mediatorLiveData.addSource(fileRepo.observeNextVersion(file.metaHash)) { nextFileVersion ->
//            if (nextFileVersion == null) {
//                mediatorLiveData.postValue(false)
//            } else {
//                mediatorLiveData.postValue(true)
//            }
//        }
//        return mediatorLiveData
//    }
//
//    fun observePreviousFileVersion(file: File): LiveData<File?> {
//        val mediatorLiveData: MediatorLiveData<File?> = MediatorLiveData<File?>()
//        if (file.previousVersionHash != null) {
//            mediatorLiveData.addSource(fileRepo.observePreviousVersion(file.previousVersionHash)) { previousFileVersion ->
//                mediatorLiveData.postValue(previousFileVersion)
//            }
//        } else {
//            mediatorLiveData.postValue(null)
//        }
//        return mediatorLiveData
//    }
//
//    fun observeNextFileVersion(file: File): LiveData<File?> {
//        val mediatorLiveData: MediatorLiveData<File?> = MediatorLiveData<File?>()
//        mediatorLiveData.addSource(fileRepo.observeNextVersion(file.contentHash)) { nextFileVersion ->
//            mediatorLiveData.postValue(nextFileVersion)
//        }
//        return mediatorLiveData
//    }
}