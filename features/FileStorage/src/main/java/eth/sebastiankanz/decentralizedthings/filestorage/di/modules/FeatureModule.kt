package eth.sebastiankanz.decentralizedthings.filestorage.di.modules

import eth.sebastiankanz.decentralizedthings.filestorage.FileStorageWorker
import org.koin.dsl.module

val featureModule = module {
    single { FileStorageWorker(get(), get(), get(), get()) }
}
