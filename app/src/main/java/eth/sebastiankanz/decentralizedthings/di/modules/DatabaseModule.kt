package eth.sebastiankanz.decentralizedthings.di.modules

import androidx.room.Room
import eth.sebastiankanz.decentralizedthings.data.IPFSDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            IPFSDatabase::class.java,
            "IPFS_DATABASE"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<IPFSDatabase>().fileDao() }
    single { get<IPFSDatabase>().encryptionBundleDao() }
}
