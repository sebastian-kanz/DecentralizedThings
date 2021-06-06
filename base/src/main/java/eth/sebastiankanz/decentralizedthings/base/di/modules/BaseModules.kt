package eth.sebastiankanz.decentralizedthings.base.di.modules

import eth.sebastiankanz.decentralizedthings.base.service.YubikeyHandler
import org.koin.dsl.module

val baseModule = module {
    single { YubikeyHandler() }
}
