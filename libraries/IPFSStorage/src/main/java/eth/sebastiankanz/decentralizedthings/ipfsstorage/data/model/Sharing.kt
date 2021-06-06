package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model and Entity class for a Sharing.
 */

@Entity(tableName = "sharings")
data class Sharing(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val pubKey: String = "",
    val IV: ByteArray = ByteArray(0),
    val encryptionBundle: EncryptionBundle = EncryptionBundle()
)