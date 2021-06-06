package eth.sebastiankanz.decentralizedthings.di.modules

import eth.sebastiankanz.decentralizedthings.DecentralizedThingsApplication
import eth.sebastiankanz.decentralizedthings.persistence.AppSettings
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val applicationModule = module {
    single { androidApplication() as DecentralizedThingsApplication }
    single { AppSettings(androidContext()) }
}
