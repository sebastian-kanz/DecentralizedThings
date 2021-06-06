package eth.sebastiankanz.decentralizedthings.base.features.filestorage.model

sealed class FileError {
    data class FeatureNotEnabledError(val msg: String = "File feature is not enabled.") : FileError()
    data class GeneralFileError(val msg: String?) : FileError()
}
