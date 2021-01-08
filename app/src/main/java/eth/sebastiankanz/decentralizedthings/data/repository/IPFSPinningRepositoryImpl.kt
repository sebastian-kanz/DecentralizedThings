package eth.sebastiankanz.decentralizedthings.data.repository

import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity
import eth.sebastiankanz.decentralizedthings.network.PinataClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class IPFSPinningRepositoryImpl(
    private val pinataClient: PinataClient
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
                if (pinataResponse.id != "") {
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
        LOGGER.info("Pinning hash $hash.")
        return withContext(Dispatchers.IO) {
            try {
                val pinataResponse = pinataClient.unPinByHash(hash)
                if (pinataResponse.string() == HTTP_OK) {
                    Either.Right(true)
                } else {
                    Either.Right(false)
                }
            } catch (e: Exception) {
                Either.Left(ErrorEntity.RepoError.PinningError(e.message))
            }
        }
    }
}