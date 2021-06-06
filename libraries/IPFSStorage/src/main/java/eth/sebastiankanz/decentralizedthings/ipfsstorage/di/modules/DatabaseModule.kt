package eth.sebastiankanz.decentralizedthings.ipfsstorage.di.modules

import androidx.room.Room
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.IPFSDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            IPFSDatabase::class.java,
            "IPFSSTORAGE_DATABASE"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<IPFSDatabase>().fileDao() }
    single { get<IPFSDatabase>().encryptionBundleDao() }
}
