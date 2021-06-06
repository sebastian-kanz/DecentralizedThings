package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository

import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import io.ipfs.api.IPFS
import io.ipfs.api.NamedStreamable
import io.ipfs.multihash.Multihash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

internal class IPFSRepositoryImpl : IPFSRepository {

    companion object {
        private const val IPFS_GATEWAY = "/dnsaddr/ipfs.infura.io/tcp/5001/https"
        private val LOGGER = Logger.getLogger("IPFSRepository")
    }

    override suspend fun upload(data: ByteArray, onlyHashCalculation: Boolean): Either<ErrorEntity, String> {
        LOGGER.info("Uploading data to IPFS.")
        return withContext(Dispatchers.IO) {
            try {
                val ipfs = IPFS(IPFS_GATEWAY)
                val tmpIPFSObject = NamedStreamable.ByteArrayWrapper("name_does_not_matter_for_upload.txt", data)
                val multihash = ipfs.add(tmpIPFSObject, false, onlyHashCalculation).get(0).hash
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
