package eth.sebastiankanz.decentralizedthings.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eth.sebastiankanz.decentralizedthings.data.converters.FileConverters
import eth.sebastiankanz.decentralizedthings.data.dao.EncryptionBundleDao
import eth.sebastiankanz.decentralizedthings.data.dao.FileDao
import eth.sebastiankanz.decentralizedthings.data.model.EncryptionBundle
import eth.sebastiankanz.decentralizedthings.data.model.File

/**
 * The Room database for Container
 *
 */
@Database(entities = [File::class, EncryptionBundle::class], version = 1, exportSchema = false)
@TypeConverters(FileConverters::class)
abstract class IPFSDatabase : RoomDatabase() {

    abstract fun fileDao(): FileDao
    abstract fun encryptionBundleDao(): EncryptionBundleDao
}