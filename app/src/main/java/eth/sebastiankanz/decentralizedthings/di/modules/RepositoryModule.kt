package eth.sebastiankanz.decentralizedthings.di.modules

import eth.sebastiankanz.decentralizedthings.data.repository.EncryptionBundleRepository
import eth.sebastiankanz.decentralizedthings.data.repository.EncryptionBundleRepositoryImpl
import eth.sebastiankanz.decentralizedthings.data.repository.FileRepository
import eth.sebastiankanz.decentralizedthings.data.repository.FileRepositoryImpl
import eth.sebastiankanz.decentralizedthings.data.repository.IPFSPinningRepository
import eth.sebastiankanz.decentralizedthings.data.repository.IPFSPinningRepositoryImpl
import eth.sebastiankanz.decentralizedthings.data.repository.IPFSRepository
import eth.sebastiankanz.decentralizedthings.data.repository.IPFSRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    factory<FileRepository> { FileRepositoryImpl(get()) }
    factory<EncryptionBundleRepository> { EncryptionBundleRepositoryImpl(get()) }
    factory<IPFSRepository> { IPFSRepositoryImpl() }
    factory<IPFSPinningRepository> { IPFSPinningRepositoryImpl(get(named("PinataClient"))) }
}
