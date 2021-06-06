package eth.sebastiankanz.decentralizedthings.ipfsstorage.di.modules

import com.yubico.yubikit.android.YubiKitManager
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.ipfs.IPFSUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.EncryptionUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.LocalStorageUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.CreateIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.GetIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.ImportIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.ManipulateIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.ShareIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.SyncIPFSObjectUseCase
import eth.sebastiankanz.decentralizedthings.ipfsstorage.domain.local.ipfsobject.UpdateChildObjectsUseCase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val useCaseModule = module {
    single { CreateIPFSObjectUseCase(get(), get(), get()) }
    single { GetIPFSObjectUseCase(get()) }
    single { ImportIPFSObjectUseCase(get(), get(), get()) }
    single { ManipulateIPFSObjectUseCase(get(), get(), get(), get(), get(), get(), get()) }
    single { UpdateChildObjectsUseCase(get(), get()) }
    single { ShareIPFSObjectUseCase() }
    single { SyncIPFSObjectUseCase(get(), get(), get()) }
    single { IPFSUseCase(get(), get(), get()) }
    single { EncryptionUseCase(get()) }
    single { YubiKitManager(androidContext()) }
    single { LocalStorageUseCase(androidApplication()) }
}
