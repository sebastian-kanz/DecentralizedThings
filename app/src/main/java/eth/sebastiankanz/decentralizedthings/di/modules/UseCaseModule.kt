package eth.sebastiankanz.decentralizedthings.di.modules

import eth.sebastiankanz.decentralizedthings.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.EncryptionUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.CreateFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.GetFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.ImportFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.ManipulateFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.ShareFileUseCase
import eth.sebastiankanz.decentralizedthings.domain.local.file.SyncFileUseCase
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

val useCaseModule = module {
    single { CreateFileUseCase(get(), get(), get()) }
    single { GetFileUseCase(get()) }
    single { ImportFileUseCase(get(), get(), get()) }
    single { ManipulateFileUseCase(get(), get(), get(), get(), get(), get()) }
    single { ShareFileUseCase() }
    single { SyncFileUseCase(get(), get(), get()) }
    single { IPFSUseCase(get(), get(), get(named("PinataClient")), get()) }
    single { EncryptionUseCase(get()) }
    single { LocalStorageUseCase(androidApplication()) }
}
