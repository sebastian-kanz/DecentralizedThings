package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository

import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity

internal interface IPFSRepository {
    suspend fun upload(data: ByteArray, onlyHashCalculation: Boolean): Either<ErrorEntity, String>
    suspend fun download(hash: String): Either<ErrorEntity, ByteArray>
}
