package eth.sebastiankanz.decentralizedthings

import android.app.Activity
import android.app.Application
import android.os.Bundle
import eth.sebastiankanz.decentralizedthings.di.modules.androidModule
import eth.sebastiankanz.decentralizedthings.di.modules.applicationModule
import eth.sebastiankanz.decentralizedthings.di.modules.databaseModule
import eth.sebastiankanz.decentralizedthings.di.modules.ipfsModule
import eth.sebastiankanz.decentralizedthings.di.modules.networkModule
import eth.sebastiankanz.decentralizedthings.di.modules.repositoryModule
import eth.sebastiankanz.decentralizedthings.di.modules.useCaseModule
import eth.sebastiankanz.decentralizedthings.di.modules.viewModelModule
import org.koin.core.KoinComponent
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DecentralizedThingsApplication : Application(), KoinComponent {

    private val mActivityLifecycleCallbacks = object : ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }

    override fun onCreate() {
        super.onCreate()
        initDI()
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    private fun initDI() {
        startKoin {
            // Android context
            androidContext(this@DecentralizedThingsApplication)
            modules(
                listOf(
                    androidModule,
                    applicationModule,
                    databaseModule,
                    ipfsModule,
                    repositoryModule,
                    useCaseModule,
                    viewModelModule,
                    networkModule
                )
            )
        }
    }
}