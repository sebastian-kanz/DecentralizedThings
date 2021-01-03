package eth.sebastiankanz.decentralizedthings.ui.storage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.domain.local.file.CreateFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.GetFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.ImportFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.ManipulateFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.ShareFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.SyncFileUseCase
import org.koin.core.KoinComponent

class StorageViewModel(

    private val createFileUseCase: CreateFileUseCase,
    private val getFileUseCase: GetFileUseCase,
    private val importFileUseCase: ImportFileUseCase,
    private val manipulateFileUseCase: ManipulateFileUseCase,
    private val shareFileUseCase: ShareFileUseCase,
    private val syncFileUseCase: SyncFileUseCase,
) : ViewModel(), KoinComponent {

    private val _showLatest = MutableLiveData<Boolean>(false)
    val latestAllFilesLiveData = Transformations.switchMap(_showLatest) { onlyLatest ->
        if (onlyLatest) {
            getFileUseCase.observeAllLatest()
        } else {
            allFilesLiveData
        }
    }

    fun showLatestFiles(showLatest: Boolean) {
        _showLatest.postValue(showLatest)
    }

    val allFilesLiveData = getFileUseCase.observeAll()

    fun createFile(fileName: String, fileType: String, fileContent: ByteArray) = createFileUseCase.create(fileContent, "$fileName.$fileType")

    fun deleteFileLocally(file: File) = manipulateFileUseCase.deleteFile(file, updateRecursive = true, onlyLocally = true)

    fun deleteFile(file: File) = manipulateFileUseCase.deleteFile(file, updateRecursive = true, onlyLocally = false)

    fun renameFileLocally(file: File, newName: String) = manipulateFileUseCase.renameFile(file, newName, updateRecursive = true, onlyLocally = true)

    fun renameFile(file: File, newName: String) = manipulateFileUseCase.renameFile(file, newName, updateRecursive = true, onlyLocally = false)

    fun syncFileContentToIPFS(file: File): LiveData<File?> {
        return syncFileUseCase.syncFileToIPFS(file)
    }

    fun syncFileContentFromIPFS(file: File): LiveData<File?> {
        return syncFileUseCase.syncFileFromIPFS(file)
    }

    fun onFileDeletedExternally(file: File?) {
        if (file != null) {
            manipulateFileUseCase.deleteFile(file = file, updateRecursive = true, onlyLocally = true)
        }
    }

    fun onFileModifiedExternally(file: File?) {
        if (file != null) {
            manipulateFileUseCase.deleteFile(file, updateRecursive = true, onlyLocally = true)
            val localFile = java.io.File(file.localPath ?: "")
            if (localFile.exists()) {
                createFileUseCase.create(localFile.readBytes(), file.name, file.localPath, true, file.metaHash, file.version, false, file.files)
            }
        }
    }
}