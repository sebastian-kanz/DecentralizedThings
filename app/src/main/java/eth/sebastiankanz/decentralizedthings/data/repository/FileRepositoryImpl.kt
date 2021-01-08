package eth.sebastiankanz.decentralizedthings.data.repository

import androidx.lifecycle.LiveData
import eth.sebastiankanz.decentralizedthings.data.dao.FileDao
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class FileRepositoryImpl(
    private val fileDao: FileDao
) : FileRepository {

    companion object {
        private val LOGGER = Logger.getLogger("FileRepository")
    }

    override suspend fun getFile(hash: String): Either<ErrorEntity, File> {
        LOGGER.info("Getting file for hash: $hash")
        return withContext(Dispatchers.IO) {
            try {
                val file = fileDao.get(hash)
                Either.Right(file)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.FileError(e.message))
            }
        }
    }

    override suspend fun getByMetaHash(metaHash: String): Either<ErrorEntity, File> {
        LOGGER.info("Getting file by meta hash: $metaHash")
        return withContext(Dispatchers.IO) {
            try {
                val file = fileDao.getByMetaHash(metaHash)
                Either.Right(file)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.FileError(e.message))
            }
        }
    }

    override suspend fun create(file: File): Either<ErrorEntity, Unit> {
        LOGGER.info("Inserting file: $file")
        return withContext(Dispatchers.IO) {
            try {
                fileDao.insert(file)
                Either.Right(Unit)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.FileError(e.message))
            }
        }
    }

    override fun observeAll(): LiveData<List<File>> {
        return fileDao.observeAll()
    }

    override suspend fun getAll(): Either<ErrorEntity, List<File>> {
        LOGGER.info("Getting all files.")
        return withContext(Dispatchers.IO) {
            try {
                val allFiles = fileDao.getAll()
                Either.Right(allFiles)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.FileError(e.message))
            }
        }
    }

    override suspend fun update(file: File): Either<ErrorEntity, Unit> {
        LOGGER.info("Updating File: $file")
        return withContext(Dispatchers.IO) {
            try {
                fileDao.update(file)
                Either.Right(Unit)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.FileError(e.message))
            }
        }
    }

    override fun observe(hash: String): LiveData<File?> {
        return fileDao.observe(hash)
    }

    override fun observeByMetaHash(metaHash: String): LiveData<File?> {
        return fileDao.observeByMetaHash(metaHash)
    }

    override suspend fun delete(file: File): Either<ErrorEntity, Unit> {
        LOGGER.info("Deleting File: $file")
        return withContext(Dispatchers.IO) {
            try {
                fileDao.delete(file)
                Either.Right(Unit)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.FileError(e.message))
            }
        }
    }

    override suspend fun exists(contentHash: String): Either<ErrorEntity, Boolean> {
        LOGGER.info("Checking if file exists with content hash: $contentHash")
        return withContext(Dispatchers.IO) {
            try {
                Either.Right(fileDao.exists(contentHash))
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.FileError(e.message))
            }
        }
    }

    override suspend fun existsByMetaHash(metaHash: String): Either<ErrorEntity, Boolean> {
        LOGGER.info("Checking if file exists with meta hash: $metaHash")
        return withContext(Dispatchers.IO) {
            try {
                Either.Right(fileDao.existsByMetaHash(metaHash))
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.FileError(e.message))
            }
        }
    }

    override fun observeNextVersion(metaHash: String): LiveData<File?> {
        return fileDao.observeNextVersion(metaHash)
    }

    override fun observePreviousVersion(previousVersionHash: String): LiveData<File?> {
        return fileDao.observePreviousVersion(previousVersionHash)
    }

    override suspend fun getParents(metaHash: String): Either<ErrorEntity, List<File>> {
        LOGGER.info("Getting parents for meta hash: $metaHash")
        return withContext(Dispatchers.IO) {
            try {
                Either.Right(fileDao.getParents(metaHash))
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.FileError(e.message))
            }
        }
    }
}
