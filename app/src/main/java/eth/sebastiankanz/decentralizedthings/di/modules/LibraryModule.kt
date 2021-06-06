package eth.sebastiankanz.decentralizedthings.di.modules

import eth.sebastiankanz.decentralizedthings.ipfsstorage.IPFSStorage
import org.koin.dsl.module

val libraryModule = module {
    single { IPFSStorage.newInstance() }
}