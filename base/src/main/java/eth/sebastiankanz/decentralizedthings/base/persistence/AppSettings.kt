package eth.sebastiankanz.decentralizedthings.base.persistence

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
        private const val PINATA_API_JWT = "PINATA_API_JWT"
        private const val INFURA_API_PROJECT_ID = "infura_api_project_id"
        private const val INFURA_API_PROJECT_SECRET = "infura_api_project_secret"
    }

    private val mSharedPreferences: SharedPreferences

    init {
        mSharedPreferences = context.getSharedPreferences(IPFS_TEST_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        if (!mSharedPreferences.contains(UNIQUE_ID)) {
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

    var pinataApiJwt: String?
        //        get() = mSharedPreferences.getString(PINATA_API_JWT, null)
        get() = mSharedPreferences.getString(
            PINATA_API_JWT,
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mb3JtYXRpb24iOnsiaWQiOiJlOWM4OTQyMi1mNjJhLTRiYzEtODkxMC00MWEyZWU1MTZjYTAiLCJlbWFpbCI6InNlYmFzdGlhbi5rYW56QGdvb2dsZW1haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsInBpbl9wb2xpY3kiOnsicmVnaW9ucyI6W3siaWQiOiJGUkExIiwiZGVzaXJlZFJlcGxpY2F0aW9uQ291bnQiOjF9XSwidmVyc2lvbiI6MX0sIm1mYV9lbmFibGVkIjpmYWxzZX0sImF1dGhlbnRpY2F0aW9uVHlwZSI6InNjb3BlZEtleSIsInNjb3BlZEtleUtleSI6IjA4Mzc2YzlmYzA0MjNjNmE1Y2QzIiwic2NvcGVkS2V5U2VjcmV0IjoiYWQ1YTMyY2FkM2E1OWQzYTQyYjAwZjQ0NWFmYjE1MTg1MmNmNzEyY2QyMTg1OGJmZjYyODgzMDk2NTQ3ZDg0ZiIsImlhdCI6MTYwODU3NjA5OH0.LB3prOCOytmEtou_qQg9JrgQVcLLr7nt3SWmOaR-t-g"
        )
        set(jwt) = mSharedPreferences.edit().putString(PINATA_API_JWT, jwt).apply()
//    const val apiSecret = "ad5a32cad3a59d3a42b00f445afb151852cf712cd21858bff62883096547d84f"
//    const val apiKey = "08376c9fc0423c6a5cd3"
    var infuraApiProjectId: String?
        //        get() = mSharedPreferences.getString(INFURA_API_PROJECT_ID, null)
        get() = mSharedPreferences.getString(INFURA_API_PROJECT_ID, "1uvprhMefLCOgMjHpJLmb1HmExw")
        set(id) = mSharedPreferences.edit().putString(INFURA_API_PROJECT_ID, id).apply()

    var infuraApiProjectSecret: String?
        //        get() = mSharedPreferences.getString(INFURA_API_PROJECT_SECRET, null)
        get() = mSharedPreferences.getString(INFURA_API_PROJECT_SECRET, "8c49e801dee34e078627b7c9080426c4")
        set(secret) = mSharedPreferences.edit().putString(INFURA_API_PROJECT_SECRET, secret).apply()
}