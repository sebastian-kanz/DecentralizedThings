package eth.sebastiankanz.decentralizedthings.data.repository

import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity
import io.ipfs.api.IPFS
import io.ipfs.api.NamedStreamable
import io.ipfs.multihash.Multihash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class IPFSRepositoryImpl : IPFSRepository {

    companion object {
        private const val IPFS_GATEWAY = "/dnsaddr/ipfs.infura.io/tcp/5001/https"
        private val LOGGER = Logger.getLogger("IPFSRepository")
    }

    override suspend fun upload(data: ByteArray, onlyHashCalculation: Boolean): Either<ErrorEntity, String> {
        LOGGER.info("Uploading data to IPFS.")
        return withContext(Dispatchers.IO) {
            try {
                val ipfs = IPFS(IPFS_GATEWAY)
                val tmpFile = NamedStreamable.ByteArrayWrapper("name_does_not_matter_for_upload.txt", data)
                val multihash = ipfs.add(tmpFile, false, onlyHashCalculation).get(0).hash
                LOGGER.info("Uploaded data to IPFS: $multihash")
                Either.Right(multihash.toString())
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSError(e.message))
            }
        }
    }

    override suspend fun download(hash: String): Either<ErrorEntity, ByteArray> {
        LOGGER.info("Downloading data from IPFS.")
        return withContext(Dispatchers.IO) {
            try {
                val ipfs = IPFS(IPFS_GATEWAY)
                val multihash = Multihash.fromBase58(hash)
                val content = ipfs.cat(multihash)
                LOGGER.info("Downloaded data from IPFS: $content")
                Either.Right(content)
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.IPFSError(e.message))
            }
        }
    }
}
