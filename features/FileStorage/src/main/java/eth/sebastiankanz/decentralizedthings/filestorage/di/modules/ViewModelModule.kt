package eth.sebastiankanz.decentralizedthings.filestorage.di.modules

import eth.sebastiankanz.decentralizedthings.filestorage.ui.FileStorageViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { FileStorageViewModel() }
}
