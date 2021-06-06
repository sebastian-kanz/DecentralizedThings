package eth.sebastiankanz.decentralizedthings.ipfsstorage.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObject
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.converters.IPFSObjectConverters
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.dao.EncryptionBundleDao
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.dao.IPFSObjectDao
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.EncryptionBundle

/**
 * The Room database for Container
 *
 */
@Database(entities = [IPFSObject::class, EncryptionBundle::class], version = 1, exportSchema = false)
@TypeConverters(IPFSObjectConverters::class)
internal abstract class IPFSDatabase : RoomDatabase() {

    abstract fun fileDao(): IPFSObjectDao
    abstract fun encryptionBundleDao(): EncryptionBundleDao
}