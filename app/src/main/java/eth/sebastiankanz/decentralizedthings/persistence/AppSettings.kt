package eth.sebastiankanz.decentralizedthings.persistence

import android.content.Context
import android.content.SharedPreferences
import org.koin.core.KoinComponent

class AppSettings(
    private val context: Context
) : KoinComponent {
    companion object {
        private const val APP_SETTINGS_ANALYTICS_ENABLED = "app_settings_analytics_enabled"
        private const val IPFS_TEST_SHARED_PREFERENCES = "ipfs_test_sharedPreferences";
    }

    private val mSharedPreferences: SharedPreferences = context.getSharedPreferences(IPFS_TEST_SHARED_PREFERENCES, Context.MODE_PRIVATE)

    fun cleanUp() {
        //remove some data
    }

    fun saveSomethingEnables(isEnabled: Boolean) {
        mSharedPreferences.edit().putBoolean(APP_SETTINGS_ANALYTICS_ENABLED, isEnabled).apply()
    }

    val isSomethingEnabled: Boolean
        get() = mSharedPreferences.getBoolean(APP_SETTINGS_ANALYTICS_ENABLED, false)
}