package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository

import androidx.lifecycle.LiveData
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.dao.IPFSObjectDao
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

internal class IPFSObjectRepositoryImpl(
    private val ipfsObjectDao: IPFSObjectDao
) : IPFSObjectRepository {

    companion object {
        private val LOGGER = Logger.getLogger("IPFSObjectRepository")
    }

    override suspend fun getIPFSObject(hash: String): Either<ErrorEntity, IPFSObject> {
        LOGGER.info("Getting ipfsObject for hash: $hash")
        return withContext(Dispatchers.IO) {
            try {
                val ipfsObject = ipfsObjectDao.get(hash)
                Either.Right(ipfsObject)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }

    override suspend fun getByMetaHash(metaHash: String): Either<ErrorEntity, IPFSObject> {
        LOGGER.info("Getting ipfsObject by meta hash: $metaHash")
        return withContext(Dispatchers.IO) {
            try {
                val ipfsObject = ipfsObjectDao.getByMetaHash(metaHash)
                Either.Right(ipfsObject)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }

    override suspend fun create(ipfsObject: IPFSObject): Either<ErrorEntity, Unit> {
        LOGGER.info("Inserting ipfsObject: $ipfsObject")
        return withContext(Dispatchers.IO) {
            try {
                ipfsObjectDao.insert(ipfsObject)
                Either.Right(Unit)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }

    override fun observeAll(): LiveData<List<IPFSObject>> {
        return ipfsObjectDao.observeAll()
    }

    override suspend fun getAll(): Either<ErrorEntity, List<IPFSObject>> {
        LOGGER.info("Getting all ipfsObjects.")
        return withContext(Dispatchers.IO) {
            try {
                val allIPFSObjects = ipfsObjectDao.getAll()
                Either.Right(allIPFSObjects)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }

    override suspend fun getRootByType(rootType: IPFSObjectType): Either<ErrorEntity, IPFSObject?> {
        LOGGER.info("Getting all root ipfsObjects.")
        return withContext(Dispatchers.IO) {
            try {
                if(rootType is IPFSObjectType.ROOT) {
                    Either.Left(ErrorEntity.RepoError.IPFSObjectError("Root type can not be of type Root."))
                }
                val objects = ipfsObjectDao.getByType(IPFSObjectType.ROOT(rootType))
                when(objects.size) {
                    0 -> Either.Right(null)
                    1 -> Either.Right(objects[0])
                    else -> Either.Left(ErrorEntity.RepoError.IPFSObjectError("Multiple roots of same type should not exist."))
                }
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }

    override suspend fun update(ipfsObject: IPFSObject): Either<ErrorEntity, Unit> {
        LOGGER.info("Updating ipfsObject: $ipfsObject")
        return withContext(Dispatchers.IO) {
            try {
                ipfsObjectDao.update(ipfsObject)
                Either.Right(Unit)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }

    override fun observe(hash: String): LiveData<IPFSObject?> {
        return ipfsObjectDao.observe(hash)
    }

    override fun observeByMetaHash(metaHash: String): LiveData<IPFSObject?> {
        return ipfsObjectDao.observeByMetaHash(metaHash)
    }

    override suspend fun delete(ipfsObject: IPFSObject): Either<ErrorEntity, Unit> {
        LOGGER.info("Deleting ipfsObject: $ipfsObject")
        return withContext(Dispatchers.IO) {
            try {
                ipfsObjectDao.delete(ipfsObject)
                Either.Right(Unit)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }

    override suspend fun exists(contentHash: String): Either<ErrorEntity, Boolean> {
        LOGGER.info("Checking if ipfsObject exists with content hash: $contentHash")
        return withContext(Dispatchers.IO) {
            try {
                Either.Right(ipfsObjectDao.exists(contentHash))
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }

    override suspend fun existsByMetaHash(metaHash: String): Either<ErrorEntity, Boolean> {
        LOGGER.info("Checking if ipfsObject exists with meta hash: $metaHash")
        return withContext(Dispatchers.IO) {
            try {
                Either.Right(ipfsObjectDao.existsByMetaHash(metaHash))
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }

    override fun observeNextVersion(metaHash: String): LiveData<IPFSObject?> {
        return ipfsObjectDao.observeNextVersion(metaHash)
    }

    override fun observePreviousVersion(previousVersionHash: String): LiveData<IPFSObject?> {
        return ipfsObjectDao.observePreviousVersion(previousVersionHash)
    }

    override suspend fun getParents(metaHash: String): Either<ErrorEntity, List<IPFSObject>> {
        LOGGER.info("Getting parents for meta hash: $metaHash")
        return withContext(Dispatchers.IO) {
            try {
                Either.Right(ipfsObjectDao.getParents(metaHash))
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSObjectError(e.message))
            }
        }
    }
}
