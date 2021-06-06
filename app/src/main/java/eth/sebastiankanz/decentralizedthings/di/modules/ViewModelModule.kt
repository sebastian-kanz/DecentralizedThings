package eth.sebastiankanz.decentralizedthings.di.modules

import eth.sebastiankanz.decentralizedthings.ui.MainActivityViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainActivityViewModel(androidApplication()) }
}
