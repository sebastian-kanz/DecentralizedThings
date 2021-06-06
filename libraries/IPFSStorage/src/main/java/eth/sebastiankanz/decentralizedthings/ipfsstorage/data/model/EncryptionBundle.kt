package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model and Entity class for a EncryptionBundle.
 */

@Entity(tableName = "encryptionbundles")
data class EncryptionBundle(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,
    val ipfsHash: String = "",
    val keyName: String = "",
    val initializationVector: ByteArray = ByteArray(0),
)

data class EncryptionDataBundle(
    val encryptionBundle: EncryptionBundle = EncryptionBundle(),
    val ciphertext: ByteArray = ByteArray(0),
)