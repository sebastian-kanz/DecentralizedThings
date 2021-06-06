package eth.sebastiankanz.decentralizedthings.filestorage.domain

import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.FileError
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.filestorage.helper.parseFileError
import eth.sebastiankanz.decentralizedthings.filestorage.helper.toFile
import eth.sebastiankanz.decentralizedthings.ipfsstorage.IPFSStorage
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType

internal class CreateFileUseCase(
    private val ipfsStorage: IPFSStorage,
) {

    suspend fun createFile(data: ByteArray, name: String): Either<FileError, File> {
        return when (val result = ipfsStorage.createIPFSObject(data, name, IPFSObjectType.FILE, override = true)) {
            is Either.Left -> {
                Either.Left(result.a.parseFileError())
            }
            is Either.Right -> {
                Either.Right(result.b.toFile())
            }
        }
    }
}