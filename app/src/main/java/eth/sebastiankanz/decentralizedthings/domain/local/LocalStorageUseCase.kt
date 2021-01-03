package eth.sebastiankanz.decentralizedthings.domain.local

import android.content.Context
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.extensions.getMetaFileNameFromFileName
import java.util.logging.Logger

class LocalStorageUseCase(
    private val context: Context
) {
    companion object {
        private val LOGGER = Logger.getLogger("LocalStorageUseCase")
    }

    fun deleteContentFile(file: File) {
        LOGGER.info("Deleting file from local storage.")
        val localFile = getContentFile(file)
        if (localFile.isDirectory && localFile.listFiles()?.size != 0) {
            LOGGER.warning("Can not delete directory locally: Directory is not empty.")
        }
        if (!localFile.delete()) {
            LOGGER.warning("Can not delete file / directory locally.")
        }
    }

    fun deleteMetaFile(file: File) {
        LOGGER.info("Deleting meta file from local storage.")
        val localFile = getMetaFile(file)
        if (!localFile.delete()) {
            LOGGER.warning("Can not delete meta file locally.")
        }
    }

    fun getFileContent(file: File): ByteArray {
        LOGGER.info("Retrieving content from local file.")
        val localFile = getContentFile(file)
        return if (!localFile.exists()) {
            LOGGER.warning("File / directory does not exist.")
            ByteArray(0)
        } else if (localFile.isFile) {
            localFile.readBytes()
        } else {
            LOGGER.info("Can not read content as this is a directory.")
            ByteArray(0)
        }
    }

    fun getFileMetaData(file: File): ByteArray {
        LOGGER.info("Retrieving meta data from local file.")
        val localFile = getMetaFile(file)
        return if (!localFile.exists()) {
            LOGGER.warning("Meta file does not exist.")
            ByteArray(0)
        } else if (localFile.isFile) {
            localFile.readBytes()
        } else {
            LOGGER.info("Can not read content as this is a directory.")
            ByteArray(0)
        }
    }

    @Throws(FileAlreadyExistsException::class)
    fun writeContent(file: File, override: Boolean, data: ByteArray = ByteArray(0)): String {
        LOGGER.info("Writing to local storage: ${file.name}")
        val localFile = getContentFile(file)
        if (localFile.exists() && !override) {
            LOGGER.severe("Asset already exists and override is disabled.")
            throw FileAlreadyExistsException(file = localFile, reason = "Asset already exists and override is disabled.")
        }
        if (data.isEmpty()) {
            localFile.mkdir()
            LOGGER.info("Directory created.")
        } else {
            localFile.writeBytes(data)
            LOGGER.info("File created.")
        }
        return localFile.path
    }

    @Throws(FileAlreadyExistsException::class)
    fun writeMetaData(file: File, override: Boolean, data: ByteArray = ByteArray(0)): String {
        LOGGER.info("Writing to local storage: ${file.name} ")
        val localFile = getMetaFile(file)
        if (localFile.exists() && !override) {
            LOGGER.severe("Asset already exists and override is disabled.")
            throw FileAlreadyExistsException(file = localFile, reason = "Asset already exists and override is disabled.")
        }
        localFile.writeBytes(data)
        LOGGER.info("Meta file created.")
        return localFile.path
    }

    fun getContentFile(file: File): java.io.File {
        LOGGER.info("Getting local file / directory ${file.name} under path ${file.localPath}.")
        var prefix = ""
        if (file.localPath != null) {
            prefix = "${file.localPath}/"
        }
        return java.io.File(context.getExternalFilesDir(null), prefix + file.name)
    }

    fun getMetaFile(file: File): java.io.File {
        LOGGER.info("Getting local meta file ${file.name} under path ${file.localMetaPath}.")
        var prefix = ""
        if (file.localMetaPath != null) {
            prefix = "${file.localMetaPath}/"
        }
        val metaFileName = file.name.getMetaFileNameFromFileName()
        return java.io.File(context.getExternalFilesDir(null), prefix + metaFileName)
    }

    fun contentExistsLocally(file: File): Boolean {
        val localFile = getContentFile(file)
        return localFile.exists()
    }

    fun metaExistsLocally(file: File): Boolean {
        val localFile = getMetaFile(file)
        return localFile.exists()
    }

//    @Throws(FileAlreadyExistsException::class, NoSuchFileException::class)
//    fun moveLocalStorage(file: File, newPath: String, override: Boolean): String {
//        LOGGER.info("Moving $file to new location: $newPath.")
//        val oldFile = getContentFile(file, null)
//        val newFile = getContentFile(file, newPath)
//        oldFile.copyTo(newFile, override)
//        return newFile.path
//    }
}