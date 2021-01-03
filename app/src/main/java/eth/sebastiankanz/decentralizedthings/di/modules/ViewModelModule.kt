package eth.sebastiankanz.decentralizedthings.di.modules

import eth.sebastiankanz.decentralizedthings.MainActivityViewModel
import eth.sebastiankanz.decentralizedthings.ui.gallery.GalleryViewModel
import eth.sebastiankanz.decentralizedthings.ui.storage.StorageDetailViewModel
import eth.sebastiankanz.decentralizedthings.ui.storage.StorageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainActivityViewModel(androidContext()) }
    viewModel { StorageViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { StorageDetailViewModel(androidContext()) }
    viewModel { GalleryViewModel(androidContext()) }
}
