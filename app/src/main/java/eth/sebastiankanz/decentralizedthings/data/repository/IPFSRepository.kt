package eth.sebastiankanz.decentralizedthings.data.repository

import eth.sebastiankanz.decentralizedthings.helpers.Either
import eth.sebastiankanz.decentralizedthings.helpers.ErrorEntity

interface IPFSRepository {
    suspend fun upload(data: ByteArray, onlyHashCalculation: Boolean): Either<ErrorEntity, String>
    suspend fun download(hash: String): Either<ErrorEntity, ByteArray>
}
