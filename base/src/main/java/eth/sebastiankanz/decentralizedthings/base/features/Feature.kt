package eth.sebastiankanz.decentralizedthings.base.features

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.util.logging.Logger

interface Feature {

    /**
     * The name of this feature.
     */
    val label: String

    var featureFragmentHost: FeatureFragmentHost?

    /**
     * Called when the feature is loaded.
     */
    fun onCreate() {}

    /**
     * Called when the feature is unloaded and it should clean all its open resources.
     */
    fun onDestroy() {}

    /**
     * Called when the feature is enabled.
     * @param appStart indicates if it is enabled during app start (true) or from feature menu (false)
     */
    fun onStart(appStart: Boolean = false) {}

    /**
     * Called when the feature is disabled.
     */
    fun onStop() {}

    fun launchFeatureFragment() {}

    interface Dependencies {
        fun getLogger(): Logger
        fun getContext(): Context
    }

    interface Provider {
        fun get(dependencies: Dependencies): Feature
    }

    data class FeatureFragmentHost(
        val activity: AppCompatActivity,
        val container: Int
    )
}