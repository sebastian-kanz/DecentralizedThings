package eth.sebastiankanz.decentralizedthings.base.features.filestorage

import androidx.lifecycle.LiveData
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilter
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilterChaining
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSorting
import eth.sebastiankanz.decentralizedthings.base.features.Worker
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.FileError
import eth.sebastiankanz.decentralizedthings.base.helpers.Either

interface IFileStorageWorker : Worker {
    fun observeAll(
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining? = null,
        fileSorting: FileSorting? = null,
    ): LiveData<List<File>>

    fun observeAllLatest(
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining? = null,
        fileSorting: FileSorting? = null,
    ): LiveData<List<File>>

    suspend fun createFile(
        fileName: String,
        fileContent: ByteArray
    ): Either<FileError, File>

    suspend fun deleteFile(
        file: File,
        onlyLocally: Boolean,
    ): Either<FileError, Unit>

    suspend fun renameFile(
        file: File,
        newName: String,
        onlyLocally: Boolean,
        override: Boolean,
    ): Either<FileError, File>


    suspend fun syncFileToIPFS(file: File): Either<FileError, File>

    suspend fun syncFileFromIPFS(file: File): Either<FileError, File>
}