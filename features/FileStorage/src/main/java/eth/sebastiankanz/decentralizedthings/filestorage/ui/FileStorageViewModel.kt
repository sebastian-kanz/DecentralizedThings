package eth.sebastiankanz.decentralizedthings.filestorage.ui

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import eth.sebastiankanz.decentralizedthings.base.data.model.SyncState
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
import eth.sebastiankanz.decentralizedthings.filestorage.domain.CreateFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.GetFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.ManipulateFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.ShareFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.SyncFileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.util.logging.Logger

internal class FileStorageViewModel(
    private val getFileUseCase: GetFileUseCase,
    private val createFileUseCase: CreateFileUseCase,
    private val manipulateFileUseCase: ManipulateFileUseCase,
    private val syncFileUseCase: SyncFileUseCase,
    private val shareFileUseCase: ShareFileUseCase,
) : ViewModel() {

    companion object {
        private val LOGGER = Logger.getLogger("StorageViewModel")
    }

    private var _isProcessing = MutableLiveData(false)
    val isProcessing: LiveData<Boolean>
        get() = _isProcessing

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
        getFileUseCase.observeAll(fileFilters.value ?: listOf(), fileFilterChaining.value, fileSorting.value)

    private val _showLatest = MutableLiveData(true)

    val latestAllFilesLiveData = Transformations.switchMap(zipLiveData(_showLatest, fileManipulators)) { zipped ->
        if (zipped.first && Features.isEnabled(FeatureId.FILE_STORAGE)) {
            getFileUseCase.observeAllLatest(zipped.second.first, zipped.second.second, zipped.second.third)
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

    fun saveQRCode(path: String, file: File) {
        viewModelScope.launch {
            shareFileUseCase.exportQRCOde(file).onFailure { handleError(it) }.onSuccess {
                val localFile = java.io.File(path, file.name + ".png")
                val out = FileOutputStream(localFile)
                it.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    fun createFile(
        fileName: String,
        fileType: String,
        fileContent: ByteArray
    ): LiveData<File?> {
        _isProcessing.postValue(true)
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            when (val result = createFileUseCase.createFile(fileContent, "$fileName.$fileType")) {
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
        if (fileToEdit.name != editedFile.name) {
            manipulateFileUseCase.renameFile(fileToEdit, editedFile.name, false, true).onFailure { handleError(it) }.onSuccess {
                if (fileToEdit.syncState == editedFile.syncState) {
                    _isProcessing.postValue(false)
                }
            }
        }
        if (fileToEdit.syncState != editedFile.syncState) {
            if (editedFile.syncState == SyncState.SYNCED) {
                syncFileUseCase.syncFromIPFS(fileToEdit).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
            } else {
                manipulateFileUseCase.deleteFile(fileToEdit, true).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
            }
        }
    }

    fun deleteFile(file: File, onlyLocally: Boolean = false) = viewModelScope.launch {
        manipulateFileUseCase.deleteFile(file, onlyLocally).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
    }

    fun renameFile(
        file: File,
        newName: String,
        onlyLocally: Boolean = false,
        override: Boolean = true,
    ) = viewModelScope.launch {
        manipulateFileUseCase.renameFile(file, newName, onlyLocally, override).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
    }

    fun syncFileContentToIPFS(file: File) = viewModelScope.launch {
        syncFileUseCase.syncToIPFS(file).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
    }

    fun syncFileContentFromIPFS(file: File) = viewModelScope.launch {
        syncFileUseCase.syncFromIPFS(file).onFailure { handleError(it) }.onSuccess { _isProcessing.postValue(false) }
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