package eth.sebastiankanz.decentralizedthings.filestorage.domain

import android.graphics.Bitmap
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.FileError
import eth.sebastiankanz.decentralizedthings.base.helpers.Either
import eth.sebastiankanz.decentralizedthings.filestorage.helper.parseFileError
import eth.sebastiankanz.decentralizedthings.ipfsstorage.IPFSStorage

class ShareFileUseCase(
    private val ipfsStorage: IPFSStorage,
) {
    suspend fun exportQRCOde(
        file: File,
    ): Either<FileError, Bitmap> {
        return when (val ipfsObjectResult = ipfsStorage.getByMetaHash(file.metaHash)) {
            is Either.Left -> Either.Left(ipfsObjectResult.a.parseFileError())
            is Either.Right -> {
                when (val result = ipfsStorage.exportIPFSObjectQRCode(ipfsObjectResult.b)) {
                    is Either.Left -> Either.Left(result.a.parseFileError())
                    is Either.Right -> Either.Right(result.b)
                }
            }
        }
    }
}
