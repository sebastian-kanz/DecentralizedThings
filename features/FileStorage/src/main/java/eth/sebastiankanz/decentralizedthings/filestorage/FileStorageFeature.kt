package eth.sebastiankanz.decentralizedthings.filestorage

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import eth.sebastiankanz.decentralizedthings.base.features.Feature
import eth.sebastiankanz.decentralizedthings.filestorage.di.modules.useCaseModule
import eth.sebastiankanz.decentralizedthings.filestorage.di.modules.viewModelModule
import eth.sebastiankanz.decentralizedthings.filestorage.ui.FileStorageFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
class FileStorageFeature(
    private val logger: Logger,
    private val rootFragment: FileStorageFragment,
    private val context: Context
) : Feature, LifecycleOwner, CoroutineScope {

    override val label: String
        get() = "Files"

    override var featureFragmentHost: Feature.FeatureFragmentHost? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate() {
        logger.info("$label created.")
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        loadKoinModules(
            listOf(useCaseModule, viewModelModule)
        )
    }

    override fun onDestroy() {
        logger.info("$label destroyed.")
        unloadKoinModules(
            listOf(useCaseModule, viewModelModule)
        )
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun onStart(appStart: Boolean) {
        logger.info("$label started.")
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onStop() {
        logger.info("$label stopped.")
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    override fun launchFeatureFragment() {
        logger.info("Launching $label fragment.")
        featureFragmentHost?.apply {
            activity.supportFragmentManager.beginTransaction().replace(container, rootFragment).commit()
        }
    }

    companion object Provider : Feature.Provider {
        override fun get(dependencies: Feature.Dependencies): Feature {
            return FileStorageFeature(dependencies.getLogger(), FileStorageFragment(), dependencies.getContext())
        }
    }
}