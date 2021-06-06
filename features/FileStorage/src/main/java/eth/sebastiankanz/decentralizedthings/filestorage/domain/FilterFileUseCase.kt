package eth.sebastiankanz.decentralizedthings.filestorage.domain

import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilter
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileFilterChaining
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSorting

internal class FilterSortFileUseCase {

    fun sortAndFilterFiles(
        allFiles: List<File>,
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining?,
        fileSorting: FileSorting?,
    ): List<File> {
        val filteredFiles = filterFiles(allFiles, fileFilters, fileFilterChaining)
        return sortFiles(filteredFiles, fileSorting)
    }

    private fun filterFiles(
        files: List<File>,
        fileFilters: List<FileFilter>,
        fileFilterChaining: FileFilterChaining?
    ): List<File> {
        return if (fileFilters.isEmpty() || fileFilterChaining == null) {
            files
        } else if (fileFilters.size == 1) {
            files.filter(fileFilters[0].get())
        } else {
            var chainedFilter: (File) -> Boolean = { true }
            fileFilters.forEach { filter ->
                chainedFilter = fileFilterChaining.apply(chainedFilter, filter.get())
            }
            return files.filter(chainedFilter)
        }
    }

    private fun sortFiles(
        files: List<File>,
        fileSorting: FileSorting?
    ): List<File> {
        return fileSorting?.apply(files) ?: files
    }
}