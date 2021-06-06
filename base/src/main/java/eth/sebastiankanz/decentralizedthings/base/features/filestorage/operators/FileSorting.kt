package eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators

import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import kotlinx.serialization.Serializable

@Serializable
sealed class FileSorting {
    abstract var order: FileSortingOrder
    abstract fun apply(files: List<File>): List<File>

    @Serializable
    class FileMetaHashSorting(
        override var order: FileSortingOrder
    ) : FileSorting() {
        override fun apply(files: List<File>): List<File> {
            return when (order) {
                FileSortingOrder.ASC -> {
                    files.sortedBy { it.metaHash }
                }
                FileSortingOrder.DESC -> {
                    files.sortedByDescending { it.metaHash }
                }
            }
        }
    }

    @Serializable
    class FileContentHashSorting(
        override var order: FileSortingOrder
    ) : FileSorting() {
        override fun apply(files: List<File>): List<File> {
            return when (order) {
                FileSortingOrder.ASC -> {
                    files.sortedBy { it.contentHash }
                }
                FileSortingOrder.DESC -> {
                    files.sortedByDescending { it.contentHash }
                }
            }
        }
    }

    @Serializable
    class FileVersionSorting(
        override var order: FileSortingOrder
    ) : FileSorting() {
        override fun apply(files: List<File>): List<File> {
            return when (order) {
                FileSortingOrder.ASC -> {
                    files.sortedBy { it.version }
                }
                FileSortingOrder.DESC -> {
                    files.sortedByDescending { it.version }
                }
            }
        }
    }

    @Serializable
    class FileNameSorting(
        override var order: FileSortingOrder
    ) : FileSorting() {
        override fun apply(files: List<File>): List<File> {
            return when (order) {
                FileSortingOrder.ASC -> {
                    files.sortedBy { it.name }
                }
                FileSortingOrder.DESC -> {
                    files.sortedByDescending { it.name }
                }
            }
        }
    }

    @Serializable
    class FileTimestampSorting(
        override var order: FileSortingOrder
    ) : FileSorting() {
        override fun apply(files: List<File>): List<File> {
            return when (order) {
                FileSortingOrder.ASC -> {
                    files.sortedBy { it.timestamp }
                }
                FileSortingOrder.DESC -> {
                    files.sortedByDescending { it.timestamp }
                }
            }
        }
    }

    @Serializable
    class FileSizeSorting(
        override var order: FileSortingOrder
    ) : FileSorting() {
        override fun apply(files: List<File>): List<File> {
            return when (order) {
                FileSortingOrder.ASC -> {
                    files.sortedBy { it.size }
                }
                FileSortingOrder.DESC -> {
                    files.sortedByDescending { it.size }
                }
            }
        }
    }

    @Serializable
    class FileSyncStateSorting(
        override var order: FileSortingOrder
    ) : FileSorting() {
        override fun apply(files: List<File>): List<File> {
            return when (order) {
                FileSortingOrder.ASC -> {
                    files.sortedBy { it.syncState.name }
                }
                FileSortingOrder.DESC -> {
                    files.sortedByDescending { it.syncState.name }
                }
            }
        }
    }

    @Serializable
    class FileIsDirectorySorting(
        override var order: FileSortingOrder
    ) : FileSorting() {
        override fun apply(files: List<File>): List<File> {
            return when (order) {
                FileSortingOrder.ASC -> {
                    files.sortedBy { it.isDirectory() }
                }
                FileSortingOrder.DESC -> {
                    files.sortedByDescending { it.isDirectory() }
                }
            }
        }
    }
}

enum class FileSortingOrder {
    ASC,
    DESC,
}