package eth.sebastiankanz.decentralizedthings

import android.app.Activity
import android.app.Application
import android.os.Bundle
import eth.sebastiankanz.decentralizedthings.base.di.modules.baseModule
import eth.sebastiankanz.decentralizedthings.di.modules.applicationModule
import eth.sebastiankanz.decentralizedthings.di.modules.libraryModule
import eth.sebastiankanz.decentralizedthings.di.modules.viewModelModule
import eth.sebastiankanz.decentralizedthings.features.Features
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
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
        Features.load()
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    private fun initDI() {
        startKoin {
            // Android context
            androidContext(this@DecentralizedThingsApplication)
            modules(
                listOf(
                    baseModule,
                    libraryModule,
                    applicationModule,
                    viewModelModule,
                )
            )
        }
    }
}