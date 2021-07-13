package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository

import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity
import eth.sebastiankanz.decentralizedthings.ipfsstorage.network.InfuraClient
import eth.sebastiankanz.decentralizedthings.ipfsstorage.network.PinataClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

internal class IPFSPinningRepositoryImpl(
    private val pinataClient: PinataClient,
    private val infuraClient: InfuraClient,
) : IPFSPinningRepository {

    companion object {
        private val LOGGER = Logger.getLogger("IPFSPinningRepository")
        private const val HTTP_OK = "OK"
    }

    override suspend fun pinByHash(hash: String, pinName: String): Either<ErrorEntity, Boolean> {
        LOGGER.info("Pinning hash $hash with name $pinName.")
        return withContext(Dispatchers.IO) {
            try {
                val pinataResponse = pinataClient.pinByHash(hash, pinName)
                val infuraResponse = infuraClient.pinByHash(hash)
                if (pinataResponse.id != "" && infuraResponse.pins.isNotEmpty()) {
                    Either.Right(true)
                } else {
                    Either.Right(false)
                }
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.PinningError(e.message))
            }
        }
    }

    override suspend fun unPinByHash(hash: String): Either<ErrorEntity, Boolean> {
        LOGGER.info("Un-pinning hash $hash.")
            return try {
                val infuraUnpinningSuccess = infuraClient.unPinByHash(hash)
                val pinataUnpinningSuccess = if (pinataClient.isPinningJobFinished(hash)) {
                    pinataClient.unPinByHash(hash)
                } else {
                    LOGGER.warning("Could not unpin from pinata as active pinning jobs stil exist.")
                    false
                }
                if (infuraUnpinningSuccess && pinataUnpinningSuccess) {
                    Either.Right(true)
                } else {
                    Either.Right(false)
                }
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.PinningError(e.message))
            }
    }
}