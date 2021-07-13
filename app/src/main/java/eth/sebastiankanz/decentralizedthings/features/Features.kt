package eth.sebastiankanz.decentralizedthings.features

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import eth.sebastiankanz.decentralizedthings.base.features.Feature
import org.koin.java.KoinJavaComponent.getKoin
import java.util.logging.Logger

/**
 * Registry of dynamic app features.
 *
 * @author MaibornWolff GmbH
 */
object Features {

//    private val repo = get<IFeatureRepository>()

    // We keep a reference of loaded [Feature]s such that they are not GC'ed.
    private val features = mutableMapOf<FeatureId, Feature>()

    /**
     * Check if a feature is loaded.
     *
     * @param feature The feature (see [FeatureId] constants).
     */
    @JvmStatic
    fun isLoaded(feature: FeatureId) = features.containsKey(feature)

    /**
     * Check if a feature is loaded and enabled.
     *
     * @param feature The feature (see [FeatureId] constants).
     * @return True if the feature is enabled, false otherwise
     */
    @JvmStatic
    fun isEnabled(feature: FeatureId) = isLoaded(feature)
//    fun isEnabled(feature: FeatureId) = isLoaded(feature) && repo.isEnabled(feature)

    /**
     * Set a feature enabled/disabled (and start/stop it if the state changed).
     *
     * @param feature The [FeatureId] to check.
     * @param enabled True if the feature should be enabled, false otherwise.
     */
    fun setEnabled(feature: FeatureId, enabled: Boolean) {
//        if (repo.isEnabled(feature) != enabled) {
//            // Start/Stop the Feature
//            features[feature]?.let {
//                if (enabled) {
//                    LOGGER.info("Enabled feature ${it.label}")
//                    it.onStart(false)
//                } else {
//                    LOGGER.info("Disabled feature ${it.label}")
//                    it.onStop()
//                }
//            }
//            // Afterwards save new state (and notify)
//            repo.setEnabled(feature, enabled)
//        }
    }

    /**
     * Get the name of a feature.
     *
     * The name is only available if the feature could be loaded.
     *
     * @param feature The [FeatureId] to check.
     * @return The name of the feature or null.
     */
    fun getLabel(feature: FeatureId): String? = features[feature]?.label

    /**
     * Load all dynamic app features.
     *
     * Should be called from the Application's onCreate method.
     * @throws IllegalArgumentException if a required feature could not be loaded.
     */
    fun load() {
        FeatureId.values().forEach {
            load(it)
        }
//        LOGGER.info(
//            "Enabled features after 'load()': " +
//                features.mapNotNull { if (repo.isEnabled(it.key)) getLabel(it.key) else null }.joinToString()
//        )
    }

    fun getEnabledFeatures(): List<FeatureId> {
        return features.filter { isEnabled(it.key) }.map { it.key }
    }

    fun setFeatureFragmentHost(feature: FeatureId, activity: AppCompatActivity, container: Int) {
        if (!isEnabled(feature)) {
            load(feature)
        }
        features[feature]?.featureFragmentHost = Feature.FeatureFragmentHost(activity, container)
    }

    fun launchFeatureRootFragment(feature: FeatureId) {
        if (!isEnabled(feature)) {
            load(feature)
        }
        features[feature]?.launchFeatureFragment()
    }

    /**
     * Load dynamic app features with the given [FeatureId].
     *
     * @param featureId The [FeatureId] of the [Feature] to load.
     * @throws IllegalArgumentException if a required feature could not be loaded.
     */
    private fun load(featureId: FeatureId) {
        val feature = load(featureId.className, featureId.optional)
        if (feature != null) {
            features[featureId] = feature

            feature.onCreate()
            // Start the Feature if it is enabled
//            if (repo.isEnabled(featureId)) {
            feature.onStart(true)
//            }
        }
    }

    /**
     * Load [Feature] class with the given name.
     *
     * @param className The name of the [Feature] class to load.
     * @param optional  True if the feature is optional (in this case no error is thrown if the class is not found).
     * @throws IllegalArgumentException if a required feature could not be loaded.
     */
    private fun load(className: String, optional: Boolean): Feature? =
        try {
            val dependencies = object : Feature.Dependencies {
                override fun getLogger(): Logger = Logger.getLogger(className)
                override fun getContext(): Context = getKoin().get<Context>()
            }
            val storageModuleProvider = Class.forName(className).kotlin.objectInstance as Feature.Provider
            storageModuleProvider.get(dependencies)
        } catch (e: ClassNotFoundException) {
            if (optional) {
                null
            } else {
                throw IllegalArgumentException("Feature $className not found.", e)
            }
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException("Feature $className is missing no-arg constructor.", e)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error while loading feature $className.", e)
        }
}