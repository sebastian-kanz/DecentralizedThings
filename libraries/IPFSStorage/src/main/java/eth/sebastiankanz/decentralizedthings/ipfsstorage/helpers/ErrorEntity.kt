package eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers

sealed class ErrorEntity {

    sealed class ApiError : ErrorEntity() {
        // .....
    }

    sealed class RepoError : ErrorEntity() {
        data class PinningError(val msg: String?) : RepoError()

        data class IPFSError(val msg: String?) : RepoError()

        data class IPFSObjectError(val msg: String?) : RepoError()

        data class EncryptionBundleError(val msg: String?) : RepoError()
    }

    sealed class UseCaseError : ErrorEntity() {

        data class UploadToIPFSFailed(val msg: String?) : UseCaseError()

        data class DownloadFromIPFSFailed(val msg: String?) : UseCaseError()

        data class CreateObjectError(val msg: String?) : UseCaseError()

        data class GetObjectError(val msg: String?) : UseCaseError()

        data class ImportObjectError(val msg: String?) : UseCaseError()

        data class ManipulateObjectError(val msg: String?) : UseCaseError()

        data class ShareObjectError(val msg: String?) : UseCaseError()

        data class SyncObjectError(val msg: String?) : UseCaseError()

        data class EncryptionError(val msg: String?) : UseCaseError()

        data class LocalStorageError(val msg: String?) : UseCaseError()
    }
}