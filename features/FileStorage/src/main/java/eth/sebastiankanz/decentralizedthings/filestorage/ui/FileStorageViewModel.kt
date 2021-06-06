package eth.sebastiankanz.decentralizedthings.filestorage.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import eth.sebastiankanz.decentralizedthings.base.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.IFileStorageWorker
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.FileError
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilter
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilterChaining
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSorting
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSortingOrder
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.base.helpers.onFailure
import eth.sebastiankanz.decentralizedthings.base.helpers.onSuccess
import eth.sebastiankanz.decentralizedthings.base.helpers.zipLiveData
import eth.sebastiankanz.decentralizedthings.features.FeatureId
import eth.sebastiankanz.decentralizedthings.features.Features
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Logger

class FileStorageViewModel : ViewModel() {

    companion object {
        private val LOGGER = Logger.getLogger("StorageViewModel")
    }

    private var _isProcessing = MutableLiveData(false)
    val isProcessing: LiveData<Boolean>
        get() = _isProcessing

    private val fileStorageWorker: IFileStorageWorker? = if (Features.isEnabled(FeatureId.FILE_STORAGE)) {
        Features.getFeatureWorker(FeatureId.FILE_STORAGE) as IFileStorageWorker
    } else {
        null
    }

    val fileFilters = MutableLiveData(
        listOf<FileFilter>(
            FileFilter.FileIsDirectoryFilter(false),
//            FileFilter.FileIsChildOfDirectory()
        )
    )

    var fileFilterChaining = MutableLiveData<FileFilterChaining?>(FileFilterChaining.OR)
    var fileSorting = MutableLiveData<FileSorting?>(FileSorting.FileNameSorting(FileSortingOrder.DESC))
    private val fileManipulators = zipLiveData(fileFilters, fileFilterChaining, fileSorting)

    val allFilesLiveData =
        fileStorageWorker?.observeAll(fileFilters.value ?: listOf(), fileFilterChaining.value, fileSorting.value) ?: MutableLiveData(emptyList())

    private val _showLatest = MutableLiveData(true)

    val latestAllFilesLiveData = Transformations.switchMap(zipLiveData(_showLatest, fileManipulators)) { zipped ->
        if (zipped.first && Features.isEnabled(FeatureId.FILE_STORAGE)) {
            fileStorageWorker?.observeAllLatest(zipped.second.first, zipped.second.second, zipped.second.third) ?: allFilesLiveData
        } else {
            allFilesLiveData
        }
    }

    private val _error = MutableLiveData<FileError?>()
    val error: LiveData<FileError?>
        get() = _error

    fun showLatestFiles(showLatest: Boolean) {
        _showLatest.postValue(showLatest)
    }

    fun createFile(
        fileName: String,
        fileType: String,
        fileContent: ByteArray
    ): LiveData<File?> {
        _isProcessing.postValue(true)
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            when (val result =
                fileStorageWorker?.let { it.createFile("$fileName.$fileType", fileContent) } ?: Either.Left(FileError.FeatureNotEnabledError())) {
                is Either.Left -> {
                    handleError(result.a)
                    _isProcessing.postValue(false)
                    emit(null)
                }
                is Either.Right -> {
                    emit(result.b)
                }
            }
        }
    }

    fun editFile(
        fileToEdit: File,
        editedFile: File
    ) = viewModelScope.launch {
        _isProcessing.postValue(true)
        fileStorageWorker?.let { worker ->
            if (fileToEdit.name != editedFile.name) {
                worker.renameFile(fileToEdit, editedFile.name, false, true).onFailure { handleError(it) }.onSuccess {
                    if (fileToEdit.syncState == editedFile.syncState) {
                        _isProcessing.postValue(false)
                    }
                }
            }
            if (fileToEdit.syncState != editedFile.syncState) {
                if (editedFile.syncState == SyncState.SYNCED) {
                    worker.syncFileFromIPFS(fileToEdit).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
                } else {
                    worker.deleteFile(fileToEdit, true).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
                }
            }
        } ?: handleError(FileError.FeatureNotEnabledError())
    }

    fun deleteFile(file: File, onlyLocally: Boolean = false) = viewModelScope.launch {
        fileStorageWorker?.let { worker ->
            worker.deleteFile(file, onlyLocally).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
        } ?: handleError(FileError.FeatureNotEnabledError())
    }

    fun renameFile(
        file: File,
        newName: String,
        onlyLocally: Boolean = false,
        override: Boolean = true,
    ) = viewModelScope.launch {
        fileStorageWorker?.let { worker ->
            worker.renameFile(file, newName, onlyLocally, override).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
        } ?: handleError(FileError.FeatureNotEnabledError())
    }

    fun syncFileContentToIPFS(file: File) = viewModelScope.launch {
        fileStorageWorker?.let { worker ->
            worker.syncFileToIPFS(file).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
        } ?: handleError(FileError.FeatureNotEnabledError())
    }

    fun syncFileContentFromIPFS(file: File) = viewModelScope.launch {
        fileStorageWorker?.let { worker ->
            worker.syncFileFromIPFS(file).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
        } ?: handleError(FileError.FeatureNotEnabledError())
    }

    fun onFileDeletedExternally(file: File?) {
//        if (file != null) {
//            viewModelScope.launch {
//                ipfsStorage.deleteItem(file = file, updateRecursive = true, onlyLocally = true).onFailure { handleError(it) }
//            }
//        }
    }

    fun onFileModifiedExternally(file: File?) {
//        if (file != null) {
//            viewModelScope.launch {
//                ipfsStorage.deleteItem(file, updateRecursive = true, onlyLocally = true).onFailure { handleError(it) }
//            }
//            val localFile = java.io.File(file.localPath ?: "")
//            if (localFile.exists()) {
//                viewModelScope.launch {
//                    ipfsStorage.createItem(localFile.readBytes(), file.name, file.localPath, true, file.metaHash, file.version, false, file.files)
//                        .onFailure { handleError(it) }
//                }
//            }
//        }
    }

    private fun handleError(error: FileError) {
        _isProcessing.postValue(true)
        _error.postValue(error)
    }
}