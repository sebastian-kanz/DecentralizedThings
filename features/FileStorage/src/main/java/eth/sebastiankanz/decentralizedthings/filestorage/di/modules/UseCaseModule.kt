package eth.sebastiankanz.decentralizedthings.filestorage.di.modules

import eth.sebastiankanz.decentralizedthings.filestorage.domain.CreateFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.FilterSortFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.GetFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.ManipulateFileUseCase
import eth.sebastiankanz.decentralizedthings.filestorage.domain.SyncFileUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { FilterSortFileUseCase() }
    single {
        GetFileUseCase(get(), get())
    }
    single { CreateFileUseCase(get()) }
    single { ManipulateFileUseCase(get()) }
    single { SyncFileUseCase(get()) }
}