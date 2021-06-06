package eth.sebastiankanz.decentralizedthings.features

enum class FeatureId(
    internal val className: String,
    internal val optional: Boolean
) {
    /** FileStore feature */
    FILE_STORAGE("eth.sebastiankanz.decentralizedthings.filestorage.FileStorageFeature\$Provider", false),
}