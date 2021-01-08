package eth.sebastiankanz.decentralizedthings.ui.storage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.domain.local.file.CreateFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.GetFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.ImportFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.ManipulateFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.ShareFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.SyncFileUseCase
import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity
import eth.sebastiankanz.decentralizedthings.helpers.onFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import java.util.logging.Logger
import kotlin.reflect.typeOf

class StorageViewModel(

    private val createFileUseCase: CreateFileUseCase,
    private val getFileUseCase: GetFileUseCase,
    private val importFileUseCase: ImportFileUseCase,
    private val manipulateFileUseCase: ManipulateFileUseCase,
    private val shareFileUseCase: ShareFileUseCase,
    private val syncFileUseCase: SyncFileUseCase,
) : ViewModel(), KoinComponent {
    
    companion object {
        private val LOGGER = Logger.getLogger("StorageViewModel")
    }

    private val _showLatest = MutableLiveData<Boolean>(false)
    val latestAllFilesLiveData = Transformations.switchMap(_showLatest) { onlyLatest ->
        if (onlyLatest) {
            getFileUseCase.observeAllLatest()
        } else {
            allFilesLiveData
        }
    }

    private val _error = MutableLiveData<ErrorEntity?>()
    val error: LiveData<ErrorEntity?>
        get() = _error


    fun showLatestFiles(showLatest: Boolean) {
        _showLatest.postValue(showLatest)
    }

    val allFilesLiveData = getFileUseCase.observeAll()

    fun createFile(fileName: String, fileType: String, fileContent: ByteArray): LiveData<File?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            when (val result = createFileUseCase.create(fileContent, "$fileName.$fileType")) {
                is Either.Left -> {
                    handleError(result.a)
                    emit(null)
                }
                is Either.Right -> {
                    emit(result.b)
                }
            }
        }
    }

    fun deleteFileLocally(file: File) = viewModelScope.launch {
        manipulateFileUseCase.deleteFile(file, updateRecursive = true, onlyLocally = true).onFailure { handleError(it) }
    }

    fun deleteFile(file: File) = viewModelScope.launch {
        manipulateFileUseCase.deleteFile(file, updateRecursive = true, onlyLocally = false).onFailure { handleError(it) }
    }

    fun renameFileLocally(file: File, newName: String): LiveData<File?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            when (val result = manipulateFileUseCase.renameFile(file, newName, updateRecursive = true, onlyLocally = true)) {
                is Either.Left -> {
                    handleError(result.a)
                    emit(null)
                }
                is Either.Right -> {
                    emit(result.b)
                }
            }
        }
    }

    fun renameFile(file: File, newName: String): LiveData<File?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            when (val result = manipulateFileUseCase.renameFile(file, newName, updateRecursive = true, onlyLocally = false)) {
                is Either.Left -> {
                    handleError(result.a)
                    emit(null)
                }
                is Either.Right -> {
                    emit(result.b)
                }
            }
        }
    }

    fun syncFileContentToIPFS(file: File): LiveData<File?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            when (val result = syncFileUseCase.syncFileToIPFS(file)) {
                is Either.Left -> {
                    handleError(result.a)
                    emit(null)
                }
                is Either.Right -> {
                    emit(result.b)
                }
            }
        }
    }

    fun syncFileContentFromIPFS(file: File): LiveData<File?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            when (val result = syncFileUseCase.syncFileFromIPFS(file)) {
                is Either.Left -> {
                    handleError(result.a)
                    emit(null)
                }
                is Either.Right -> {
                    emit(result.b)
                }
            }
        }
    }

    fun onFileDeletedExternally(file: File?) {
        if (file != null) {
            viewModelScope.launch {
                manipulateFileUseCase.deleteFile(file = file, updateRecursive = true, onlyLocally = true).onFailure { handleError(it) }
            }
        }
    }

    fun onFileModifiedExternally(file: File?) {
        if (file != null) {
            viewModelScope.launch {
                manipulateFileUseCase.deleteFile(file, updateRecursive = true, onlyLocally = true).onFailure { handleError(it) }
            }
            val localFile = java.io.File(file.localPath ?: "")
            if (localFile.exists()) {
                viewModelScope.launch {
                    createFileUseCase.create(localFile.readBytes(), file.name, file.localPath, true, file.metaHash, file.version, false, file.files)
                        .onFailure { handleError(it) }
                }
            }
        }
    }

    private fun handleError(error: ErrorEntity) {
        
        when(error) {
            is ErrorEntity.UseCaseError.UploadToIPFSFailed -> _error.postValue(error)
            is ErrorEntity.UseCaseError.DownloadFromIPFSFailed -> _error.postValue(error)
            is ErrorEntity.UseCaseError.CreateFileError -> _error.postValue(error)
            is ErrorEntity.UseCaseError.GetFileError -> _error.postValue(error)
            is ErrorEntity.UseCaseError.ImportFileError -> _error.postValue(error)
            is ErrorEntity.UseCaseError.ManipulateFileError -> _error.postValue(error)
            is ErrorEntity.UseCaseError.ShareFileError -> _error.postValue(error)
            is ErrorEntity.UseCaseError.SyncFileError -> _error.postValue(error)
            is ErrorEntity.UseCaseError.EncryptionError -> _error.postValue(error)
            is ErrorEntity.UseCaseError.LocalStorageError -> _error.postValue(error)
            is ErrorEntity.RepoError.PinningError -> _error.postValue(error)
            is ErrorEntity.RepoError.IPFSError -> _error.postValue(error)
            is ErrorEntity.RepoError.FileError -> _error.postValue(error)
            is ErrorEntity.RepoError.EncryptionBundleError -> _error.postValue(error)
        }
    }
}