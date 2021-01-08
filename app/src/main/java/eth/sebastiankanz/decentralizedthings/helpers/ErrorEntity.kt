package eth.sebastiankanz.decentralizedthings.helpers

import androidx.lifecycle.LiveData
import eth.sebastiankanz.decentralizedthings.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.data.model.File

sealed class ErrorEntity {

    sealed class ApiError : ErrorEntity() {
        // .....
    }

    sealed class RepoError : ErrorEntity() {
        data class PinningError(val msg: String?) : RepoError()

        data class IPFSError(val msg: String?) : RepoError()

        data class FileError(val msg: String?) : RepoError()

        data class EncryptionBundleError(val msg: String?) : RepoError()
    }

    sealed class UseCaseError : ErrorEntity() {

        data class UploadToIPFSFailed(val msg: String?) : UseCaseError()

        data class DownloadFromIPFSFailed(val msg: String?) : UseCaseError()

        data class CreateFileError(val msg: String?) : UseCaseError()

        data class GetFileError(val msg: String?) : UseCaseError()

        data class ImportFileError(val msg: String?) : UseCaseError()

        data class ManipulateFileError(val msg: String?) : UseCaseError()

        data class ShareFileError(val msg: String?) : UseCaseError()

        data class SyncFileError(val msg: String?) : UseCaseError()

        data class EncryptionError(val msg: String?) : UseCaseError()

        data class LocalStorageError(val msg: String?) : UseCaseError()
    }
}