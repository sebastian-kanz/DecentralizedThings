package eth.sebastiankanz.decentralizedthings.filestorage.domain

import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.FileError
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.filestorage.helper.parseFileError
import eth.sebastiankanz.decentralizedthings.filestorage.helper.toDirectory
import eth.sebastiankanz.decentralizedthings.filestorage.helper.toFile
import eth.sebastiankanz.decentralizedthings.ipfsstorage.IPFSStorage
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject

class ManipulateFileUseCase(
    private val ipfsStorage: IPFSStorage,
) {

    suspend fun deleteFile(
        file: File,
        onlyLocally: Boolean = false
    ): Either<FileError, Unit> {
        return when (val ipfsObjectResult = ipfsStorage.getByMetaHash(file.metaHash)) {
            is Either.Left -> Either.Left(ipfsObjectResult.a.parseFileError())
            is Either.Right -> {
                when (val result = ipfsStorage.deleteIPFSObject(ipfsObjectResult.b, onlyLocally)) {
                    is Either.Left -> Either.Left(result.a.parseFileError())
                    is Either.Right -> result
                }
            }
        }
    }

    suspend fun renameFile(
        file: File,
        newName: String,
        onlyLocally: Boolean = false,
        override: Boolean = false,
    ): Either<FileError, File> {
        return when (val ipfsObjectResult = ipfsStorage.getByMetaHash(file.metaHash)) {
            is Either.Left -> Either.Left(ipfsObjectResult.a.parseFileError())
            is Either.Right -> {
                when (val result = ipfsStorage.renameIPFSObject(ipfsObjectResult.b, newName, onlyLocally, override)) {
                    is Either.Left -> Either.Left(result.a.parseFileError())
                    is Either.Right -> Either.Right(result.b.toFile())
                }
            }
        }
    }

}