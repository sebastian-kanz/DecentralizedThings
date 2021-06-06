package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository

import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity

internal interface IPFSPinningRepository {
    suspend fun pinByHash(hash: String, pinName: String): Either<ErrorEntity, Boolean>
    suspend fun unPinByHash(hash: String): Either<ErrorEntity, Boolean>
}