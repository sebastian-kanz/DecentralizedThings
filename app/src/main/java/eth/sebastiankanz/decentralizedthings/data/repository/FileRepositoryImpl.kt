package eth.sebastiankanz.decentralizedthings.data.repository

import androidx.lifecycle.LiveData
import eth.sebastiankanz.decentralizedthings.data.dao.FileDao
import eth.sebastiankanz.decentralizedthings.data.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class FileRepositoryImpl(
    private val fileDao: FileDao
) : FileRepository {

    companion object {
        private val LOGGER = Logger.getLogger("FileRepository")
    }

    override fun getFile(hash: String): File? {
        return fileDao.get(hash)
    }

    override fun getByMetaHash(metaHash: String): File? {
        return fileDao.getByMetaHash(metaHash)
    }

    override fun create(file: File) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                LOGGER.info("Inserting file: $file")
                fileDao.insert(file)
            }
        }
    }

    override fun observeAll(): LiveData<List<File>> {
        return fileDao.observeAll()
    }

    override fun getAll(): List<File> {
        return fileDao.getAll()
    }

    override fun update(updatedFile: File) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                LOGGER.info("Updating File: $updatedFile")
                fileDao.update(updatedFile)
            }
        }
    }

    override fun observe(hash: String): LiveData<File?> {
        return fileDao.observe(hash)
    }

    override fun observeByMetaHash(metaHash: String): LiveData<File?> {
        return fileDao.observeByMetaHash(metaHash)
    }

    override fun delete(file: File) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                LOGGER.info("Deleting File: $file")
                fileDao.delete(file)
            }
        }
    }

    override fun exists(contentHash: String): Boolean {
        return fileDao.exists(contentHash)
    }

    override fun existsByMetaHash(metaHash: String): Boolean {
        return fileDao.existsByMetaHash(metaHash)
    }

    override fun observeNextVersion(metaHash: String): LiveData<File?> {
        return fileDao.observeNextVersion(metaHash)
    }

    override fun observePreviousVersion(previousVersionHash: String): LiveData<File?> {
        return fileDao.observePreviousVersion(previousVersionHash)
    }

    override fun getParents(metaHash: String): List<File> {
        return fileDao.observeParents(metaHash)
    }
}
