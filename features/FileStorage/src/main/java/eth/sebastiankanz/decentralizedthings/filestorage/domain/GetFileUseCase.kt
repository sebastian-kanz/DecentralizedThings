package eth.sebastiankanz.decentralizedthings.filestorage.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.FileError
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilter
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilterChaining
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSorting
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.filestorage.helper.parseFileError
import eth.sebastiankanz.decentralizedthings.filestorage.helper.toFile
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.IPFSStorage
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType

internal class GetFileUseCase(
    private val filterSortFileUseCase: FilterSortFileUseCase,
    private val ipfsStorage: IPFSStorage,
) {
    fun observeAll(
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining?,
        fileSorting: FileSorting?
    ): LiveData<List<File>> {
        return observeAll(ipfsStorage.observeAllByType(IPFSObjectType.FILE), fileFilters, fileFilterChaining, fileSorting)
    }

    fun observeAllLatest(
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining?,
        fileSorting: FileSorting?
    ): LiveData<List<File>> {
        return observeAll(ipfsStorage.observeAllLatestByType(IPFSObjectType.FILE), fileFilters, fileFilterChaining, fileSorting)
    }

    private fun observeAll(
        observable: LiveData<List<IPFSObject>>,
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining?,
        fileSorting: FileSorting?
    ): LiveData<List<File>> {
        val filteredSortedFiles = MediatorLiveData<List<File>>()
        filteredSortedFiles.addSource(observable) { allObjects ->
            val allFiles = allObjects.map { it.toFile() }.filter { !it.isDirectory() }
            filteredSortedFiles.postValue(filterSortFileUseCase.sortAndFilterFiles(allFiles, fileFilters, fileFilterChaining, fileSorting))
        }
        return filteredSortedFiles
    }
}