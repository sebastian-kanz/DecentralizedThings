package eth.sebastiankanz.decentralizedthings.data.repository

import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity

interface IPFSPinningRepository {
    suspend fun pinByHash(hash: String, pinName: String): Either<ErrorEntity, Boolean>
    suspend fun unPinByHash(hash: String): Either<ErrorEntity, Boolean>
}