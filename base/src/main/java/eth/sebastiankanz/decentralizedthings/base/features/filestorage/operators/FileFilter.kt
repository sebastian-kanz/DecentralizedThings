package eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators

import eth.sebastiankanz.decentralizedthings.base.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File

sealed class FileFilter(protected val range: FileFilterRange) {
    abstract fun get(): (File) -> Boolean

    class FileMetaHashFilter(
        private val metaHash: String,
        range: FileFilterRange = FileFilterRange.EQ
    ) : FileFilter(range) {
        override fun get(): (File) -> Boolean {
            return when (range) {
                FileFilterRange.EQ -> {
                    { it.metaHash == metaHash }
                }
                FileFilterRange.NOT -> {
                    { it.metaHash != metaHash }
                }
                FileFilterRange.GT -> {
                    { false }
                }
                FileFilterRange.GTE -> {
                    { false }
                }
                FileFilterRange.LT -> {
                    { false }
                }
                FileFilterRange.LTE -> {
                    { false }
                }
                FileFilterRange.CONTAINS -> {
                    { it.metaHash.contains(metaHash) }
                }
                FileFilterRange.STARTS_WITH -> {
                    { it.metaHash.startsWith(metaHash) }
                }
                FileFilterRange.ENDS_WITH -> {
                    { it.metaHash.endsWith(metaHash) }
                }
            }
        }
    }

    class FileContentHashFilter(
        private val contentHash: String,
        range: FileFilterRange = FileFilterRange.EQ
    ) : FileFilter(range) {
        override fun get(): (File) -> Boolean {
            return when (range) {
                FileFilterRange.EQ -> {
                    { it.contentHash == contentHash }
                }
                FileFilterRange.NOT -> {
                    { it.contentHash != contentHash }
                }
                FileFilterRange.GT -> {
                    { false }
                }
                FileFilterRange.GTE -> {
                    { false }
                }
                FileFilterRange.LT -> {
                    { false }
                }
                FileFilterRange.LTE -> {
                    { false }
                }
                FileFilterRange.CONTAINS -> {
                    { it.contentHash.contains(contentHash) }
                }
                FileFilterRange.STARTS_WITH -> {
                    { it.contentHash.startsWith(contentHash) }
                }
                FileFilterRange.ENDS_WITH -> {
                    { it.contentHash.endsWith(contentHash) }
                }
            }
        }
    }

    class FileVersionFilter(
        private val version: Int,
        range: FileFilterRange = FileFilterRange.EQ
    ) : FileFilter(range) {
        override fun get(): (File) -> Boolean {
            return when (range) {
                FileFilterRange.EQ -> {
                    { it.version == version }
                }
                FileFilterRange.NOT -> {
                    { it.version != version }
                }
                FileFilterRange.GT -> {
                    { it.version > version }
                }
                FileFilterRange.GTE -> {
                    { it.version >= version }
                }
                FileFilterRange.LT -> {
                    { it.version < version }
                }
                FileFilterRange.LTE -> {
                    { it.version <= version }
                }
                FileFilterRange.CONTAINS -> {
                    { it.version.toString().contains(version.toString()) }
                }
                FileFilterRange.STARTS_WITH -> {
                    { it.version.toString().startsWith(version.toString()) }
                }
                FileFilterRange.ENDS_WITH -> {
                    { it.version.toString().endsWith(version.toString()) }
                }
            }
        }
    }

    class FileNameFilter(
        private val name: String,
        range: FileFilterRange = FileFilterRange.EQ
    ) : FileFilter(range) {
        override fun get(): (File) -> Boolean {
            return when (range) {
                FileFilterRange.EQ -> {
                    { it.name == name }
                }
                FileFilterRange.NOT -> {
                    { it.name != name }
                }
                FileFilterRange.GT -> {
                    { it.name.length > name.length }
                }
                FileFilterRange.GTE -> {
                    { it.name.length >= name.length }
                }
                FileFilterRange.LT -> {
                    { it.name.length < name.length }
                }
                FileFilterRange.LTE -> {
                    { it.name.length <= name.length }
                }
                FileFilterRange.CONTAINS -> {
                    { it.name.contains(name) }
                }
                FileFilterRange.STARTS_WITH -> {
                    { it.name.startsWith(name) }
                }
                FileFilterRange.ENDS_WITH -> {
                    { it.name.endsWith(name) }
                }
            }
        }
    }

    class FileTimestampFilter(
        private val timestamp: Long,
        range: FileFilterRange = FileFilterRange.EQ
    ) : FileFilter(range) {
        override fun get(): (File) -> Boolean {
            return when (range) {
                FileFilterRange.EQ -> {
                    { it.timestamp == timestamp }
                }
                FileFilterRange.NOT -> {
                    { it.timestamp != timestamp }
                }
                FileFilterRange.GT -> {
                    { it.timestamp > timestamp }
                }
                FileFilterRange.GTE -> {
                    { it.timestamp >= timestamp }
                }
                FileFilterRange.LT -> {
                    { it.timestamp < timestamp }
                }
                FileFilterRange.LTE -> {
                    { it.timestamp <= timestamp }
                }
                FileFilterRange.CONTAINS -> {
                    { it.timestamp.toString().contains(timestamp.toString()) }
                }
                FileFilterRange.STARTS_WITH -> {
                    { it.timestamp.toString().startsWith(timestamp.toString()) }
                }
                FileFilterRange.ENDS_WITH -> {
                    { it.timestamp.toString().endsWith(timestamp.toString()) }
                }
            }
        }
    }

    class FileSizeFilter(
        private val size: Long,
        range: FileFilterRange = FileFilterRange.EQ
    ) : FileFilter(range) {
        override fun get(): (File) -> Boolean {
            return when (range) {
                FileFilterRange.EQ -> {
                    { it.size == size }
                }
                FileFilterRange.NOT -> {
                    { it.size != size }
                }
                FileFilterRange.GT -> {
                    { it.size > size }
                }
                FileFilterRange.GTE -> {
                    { it.size >= size }
                }
                FileFilterRange.LT -> {
                    { it.size < size }
                }
                FileFilterRange.LTE -> {
                    { it.size <= size }
                }
                FileFilterRange.CONTAINS -> {
                    { it.size.toString().contains(size.toString()) }
                }
                FileFilterRange.STARTS_WITH -> {
                    { it.size.toString().startsWith(size.toString()) }
                }
                FileFilterRange.ENDS_WITH -> {
                    { it.size.toString().endsWith(size.toString()) }
                }
            }
        }
    }

    class FileSyncStateFilter(
        private val syncState: SyncState,
        range: FileFilterRange = FileFilterRange.EQ
    ) : FileFilter(range) {
        override fun get(): (File) -> Boolean {
            return when (range) {
                FileFilterRange.EQ -> {
                    { it.syncState == syncState }
                }
                FileFilterRange.NOT -> {
                    { it.syncState != syncState }
                }
                FileFilterRange.GT -> {
                    { it.syncState.state > syncState.state }
                }
                FileFilterRange.GTE -> {
                    { it.syncState.state >= syncState.state }
                }
                FileFilterRange.LT -> {
                    { it.syncState.state < syncState.state }
                }
                FileFilterRange.LTE -> {
                    { it.syncState.state <= syncState.state }
                }
                FileFilterRange.CONTAINS -> {
                    { false }
                }
                FileFilterRange.STARTS_WITH -> {
                    { false }
                }
                FileFilterRange.ENDS_WITH -> {
                    { false }
                }
            }
        }
    }

    class FileIsDirectoryFilter(
        private val isDirectory: Boolean,
        range: FileFilterRange = FileFilterRange.EQ
    ) : FileFilter(range) {
        override fun get(): (File) -> Boolean {
            return when (range) {
                FileFilterRange.EQ -> {
                    { it.isDirectory() == isDirectory }
                }
                FileFilterRange.NOT -> {
                    { it.isDirectory() != isDirectory }
                }
                FileFilterRange.GT -> {
                    { false }
                }
                FileFilterRange.GTE -> {
                    { false }
                }
                FileFilterRange.LT -> {
                    { false }
                }
                FileFilterRange.LTE -> {
                    { false }
                }
                FileFilterRange.CONTAINS -> {
                    { false }
                }
                FileFilterRange.STARTS_WITH -> {
                    { false }
                }
                FileFilterRange.ENDS_WITH -> {
                    { false }
                }
            }
        }
    }

    class FileIsChildOfDirectory(
        private val directory: File,
        range: FileFilterRange = FileFilterRange.EQ
    ) : FileFilter(range) {
        override fun get(): (File) -> Boolean {
            return when (range) {
                FileFilterRange.EQ -> {
                    { directory.files.any { child -> child.metaHash == it.metaHash } }
                }
                FileFilterRange.NOT -> {
                    { directory.files.none { child -> child.metaHash == it.metaHash } }
                }
                FileFilterRange.GT -> {
                    { false }
                }
                FileFilterRange.GTE -> {
                    { false }
                }
                FileFilterRange.LT -> {
                    { false }
                }
                FileFilterRange.LTE -> {
                    { false }
                }
                FileFilterRange.CONTAINS -> {
                    { false }
                }
                FileFilterRange.STARTS_WITH -> {
                    { false }
                }
                FileFilterRange.ENDS_WITH -> {
                    { false }
                }
            }
        }
    }
}

enum class FileFilterRange {
    EQ,
    NOT,
    GT,
    GTE,
    LT,
    LTE,
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
}

sealed class FileFilterChaining {
    abstract fun apply(pred1: (File) -> Boolean, pred2: (File) -> Boolean): (File) -> Boolean

    object AND : FileFilterChaining() {
        override fun apply(pred1: (File) -> Boolean, pred2: (File) -> Boolean): (File) -> Boolean {
            return { elem: File -> pred1(elem) && pred2(elem) }
        }
    }

    object OR : FileFilterChaining() {
        override fun apply(pred1: (File) -> Boolean, pred2: (File) -> Boolean): (File) -> Boolean {
            return { elem: File -> pred1(elem) || pred2(elem) }
        }
    }

    object NOR : FileFilterChaining() {
        override fun apply(pred1: (File) -> Boolean, pred2: (File) -> Boolean): (File) -> Boolean {
            return { elem: File -> !(pred1(elem) || pred2(elem)) }
        }
    }

    object XOR : FileFilterChaining() {
        override fun apply(pred1: (File) -> Boolean, pred2: (File) -> Boolean): (File) -> Boolean {
            return { elem: File -> pred1(elem) xor pred2(elem) }
        }
    }

    object XNOR : FileFilterChaining() {
        override fun apply(pred1: (File) -> Boolean, pred2: (File) -> Boolean): (File) -> Boolean {
            return { elem: File -> !(pred1(elem) xor pred2(elem)) }
        }
    }

    object NAND : FileFilterChaining() {
        override fun apply(pred1: (File) -> Boolean, pred2: (File) -> Boolean): (File) -> Boolean {
            return { elem: File -> !(pred1(elem) && pred2(elem)) }
        }
    }
}