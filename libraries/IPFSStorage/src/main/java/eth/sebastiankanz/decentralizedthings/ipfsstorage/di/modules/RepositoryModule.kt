package eth.sebastiankanz.decentralizedthings.ipfsstorage.di.modules

import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.EncryptionBundleRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.EncryptionBundleRepositoryImpl
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSObjectRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSObjectRepositoryImpl
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSPinningRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSPinningRepositoryImpl
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSRepository
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.repository.IPFSRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val repositoryModule = module {
    factory<IPFSObjectRepository> { IPFSObjectRepositoryImpl(get()) }
    factory<EncryptionBundleRepository> { EncryptionBundleRepositoryImpl(get()) }
    factory<IPFSRepository> { IPFSRepositoryImpl() }
    factory<IPFSPinningRepository> { IPFSPinningRepositoryImpl(get(named("PinataClient")), get(named("InfuraClient"))) }
}
