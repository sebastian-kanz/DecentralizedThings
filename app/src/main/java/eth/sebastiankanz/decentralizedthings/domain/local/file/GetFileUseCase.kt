package eth.sebastiankanz.decentralizedthings.domain.local.file

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.repository.FileRepository
import java.util.logging.Logger

/**
 * Use case for handling files locally and remotely on ipfs.
 * @author Sebastian Kanz
 */
class GetFileUseCase(
    private val fileRepo: FileRepository
) {

    companion object {
        private val LOGGER = Logger.getLogger("FileUseCase")
    }

    fun observeAll() = fileRepo.observeAll()

    fun getAll() = fileRepo.getAll()

    fun observe(hash: String) = fileRepo.observe(hash)

    fun observeAllLatest(): LiveData<List<File>> {
        val mediatorLiveData: MediatorLiveData<List<File>> = MediatorLiveData<List<File>>()
        mediatorLiveData.addSource(fileRepo.observeAll()) { allFiles ->
            val latestVersionFiles = allFiles.filter { outer -> allFiles.none { inner -> inner.previousVersionHash == outer.metaHash } }
            mediatorLiveData.postValue(latestVersionFiles)
        }
        return mediatorLiveData
    }

    fun hasPreviousVersion(file: File): Boolean {
        if (file.previousVersionHash != null) {
            if (file.previousVersionHash.isNotBlank() && file.previousVersionHash.isNotEmpty()) {
                return true
            }
        }
        return false
    }

    fun observeHasNextVersion(file: File): LiveData<Boolean> {
        val mediatorLiveData: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>()
        mediatorLiveData.addSource(fileRepo.observeNextVersion(file.metaHash)) { nextFileVersion ->
            if (nextFileVersion == null) {
                mediatorLiveData.postValue(false)
            } else {
                mediatorLiveData.postValue(true)
            }
        }
        return mediatorLiveData
    }

    fun observePreviousFileVersion(file: File): LiveData<File?> {
        val mediatorLiveData: MediatorLiveData<File?> = MediatorLiveData<File?>()
        if (file.previousVersionHash != null) {
            mediatorLiveData.addSource(fileRepo.observePreviousVersion(file.previousVersionHash)) { previousFileVersion ->
                mediatorLiveData.postValue(previousFileVersion)
            }
        } else {
            mediatorLiveData.postValue(null)
        }
        return mediatorLiveData
    }

    fun observeNextFileVersion(file: File): LiveData<File?> {
        val mediatorLiveData: MediatorLiveData<File?> = MediatorLiveData<File?>()
        mediatorLiveData.addSource(fileRepo.observeNextVersion(file.contentHash)) { nextFileVersion ->
            mediatorLiveData.postValue(nextFileVersion)
        }
        return mediatorLiveData
    }
}