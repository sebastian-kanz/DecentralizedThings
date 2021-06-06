package eth.sebastiankanz.decentralizedthings.persistence

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings

class AppSettings(
    private val context: Context
) {
    companion object {
        private const val IPFS_TEST_SHARED_PREFERENCES = "decentralizedthings_sharedPreferences"
        private const val UNIQUE_ID = "decentralizedthings_uuid"
        private const val APP_SETTINGS_ANALYTICS_ENABLED = "app_settings_analytics_enabled"
    }


    private val mSharedPreferences: SharedPreferences

    init {
        mSharedPreferences = context.getSharedPreferences(IPFS_TEST_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        if(!mSharedPreferences.contains(UNIQUE_ID)) {
            mSharedPreferences.edit().putString(UNIQUE_ID, Settings.Secure.ANDROID_ID).apply()
        }
//        EncryptedSharedPreferences
    }

    fun cleanUp() {
        //remove some data
    }

    fun saveSomethingEnables(isEnabled: Boolean) {
        mSharedPreferences.edit().putBoolean(APP_SETTINGS_ANALYTICS_ENABLED, isEnabled).apply()
    }

    val isSomethingEnabled: Boolean
        get() = mSharedPreferences.getBoolean(APP_SETTINGS_ANALYTICS_ENABLED, false)
}