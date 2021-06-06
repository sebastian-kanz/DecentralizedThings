package eth.sebastiankanz.decentralizedthings.filestorage.helper

import eth.sebastiankanz.decentralizedthings.base.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.FileError
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.helpers.ErrorEntity

internal fun IPFSObject.parseSyncState(): SyncState {
    return try {
        SyncState.valueOf(syncState.name)
    } catch (e: IllegalArgumentException) {
        SyncState.NONE
    }
}

internal fun ErrorEntity.parseFileError(): FileError {
    return when(this) {
        is ErrorEntity.ApiError -> FileError.GeneralFileError(this.toString())
        is ErrorEntity.RepoError.PinningError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.RepoError.IPFSError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.RepoError.IPFSObjectError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.RepoError.EncryptionBundleError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.UploadToIPFSFailed -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.DownloadFromIPFSFailed -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.CreateObjectError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.GetObjectError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.ImportObjectError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.ManipulateObjectError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.ShareObjectError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.SyncObjectError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.EncryptionError -> FileError.GeneralFileError(this.msg)
        is ErrorEntity.UseCaseError.LocalStorageError -> FileError.GeneralFileError(this.msg)
    }
}