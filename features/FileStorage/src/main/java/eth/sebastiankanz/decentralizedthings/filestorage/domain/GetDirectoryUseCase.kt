package eth.sebastiankanz.decentralizedthings.filestorage.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilter
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilterChaining
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSorting
import eth.sebastiankanz.decentralizedthings.filestorage.helper.toDirectory
import eth.sebastiankanz.decentralizedthings.ipfsstorage.IPFSStorage
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType

internal class GetDirectoryUseCase(
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

    fun getAllFilesOfDirectory(directory: IPFSObject): List<File> {
        return emptyList()
    }

    private fun observeAll(
        observable: LiveData<List<IPFSObject>>,
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining?,
        fileSorting: FileSorting?
    ): LiveData<List<File>> {
        val filteredSortedDirectories = MediatorLiveData<List<File>>()
        filteredSortedDirectories.addSource(observable) { allObjects ->
            val allDirectories = allObjects.map {
                val allFiles = getAllFilesOfDirectory(it)
                it.toDirectory(allFiles)
            }.filter { it.isDirectory() }
            filteredSortedDirectories.postValue(filterSortFileUseCase.sortAndFilterFiles(allDirectories, fileFilters, fileFilterChaining, fileSorting))
        }
        return filteredSortedDirectories
    }
}