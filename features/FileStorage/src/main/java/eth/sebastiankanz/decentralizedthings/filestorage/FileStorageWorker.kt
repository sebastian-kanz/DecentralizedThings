package eth.sebastiankanz.decentralizedthings.filestorage

import eth.sebastiankanz.decentralizedthings.base.features.filestorage.IFileStorageWorker
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.FileError
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilter
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilterChaining
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSorting
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.filestorage.domain.CreateFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.GetFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.ManipulateFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.SyncFileUseCase

internal class FileStorageWorker(
    private val getFileUseCase: GetFileUseCase,
    private val createFileUseCase: CreateFileUseCase,
    private val manipulateFileUseCase: ManipulateFileUseCase,
    private val syncFileUseCase: SyncFileUseCase,
) : IFileStorageWorker {
    override fun observeAll(
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining?,
        fileSorting: FileSorting?
    ) = getFileUseCase.observeAll(fileFilters, fileFilterChaining, fileSorting)

    override fun observeAllLatest(
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining?,
        fileSorting: FileSorting?
    ) = getFileUseCase.observeAllLatest(fileFilters, fileFilterChaining, fileSorting)

    override suspend fun createFile(fileName: String, fileContent: ByteArray) = createFileUseCase.createFile(fileContent, fileName)

    override suspend fun deleteFile(file: File, onlyLocally: Boolean) = manipulateFileUseCase.deleteFile(file, onlyLocally)

    override suspend fun renameFile(file: File, newName: String, onlyLocally: Boolean, override: Boolean) = manipulateFileUseCase.renameFile(file, newName, onlyLocally, override)

    override suspend fun syncFileToIPFS(file: File) = syncFileUseCase.syncToIPFS(file)

    override suspend fun syncFileFromIPFS(file: File) = syncFileUseCase.syncFromIPFS(file)
}