package eth.sebastiankanz.decentralizedthings.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eth.sebastiankanz.decentralizedthings.data.model.SyncState

class FileConverters {

    val gson = Gson()

    @TypeConverter
    fun toSyncState(state: Int): SyncState? {
        return if (state == SyncState.NONE.state) {
            SyncState.NONE
        } else if (state == SyncState.SYNCED.state) {
            SyncState.SYNCED
        } else if (state == SyncState.UNSYNCED_ONLY_LOCAL.state) {
            SyncState.UNSYNCED_ONLY_LOCAL
        } else if (state == SyncState.UNSYNCED_ONLY_REMOTE.state) {
            SyncState.UNSYNCED_ONLY_REMOTE
        } else if (state == SyncState.UNSYNCED_ONLY_PARTLY.state) {
            SyncState.UNSYNCED_ONLY_PARTLY
        } else {
            throw IllegalArgumentException("Could not recognize status")
        }
    }

    @TypeConverter
    fun toInteger(syncState: SyncState): Int? {
        return syncState.state
    }

    @TypeConverter
    fun StringListToJson(children: List<String>): String {
        return gson.toJson(children)
    }

    @TypeConverter
    fun jsonToStringList(json: String): List<String> {
        return gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun pairListToJson(children: List<Pair<String, String>>): String {
        return gson.toJson(children)
    }

    @TypeConverter
    fun jsonToPairList(json: String): List<Pair<String, String>> {
        return gson.fromJson(json, object : TypeToken<List<Pair<String, String>>>() {}.type)
    }
}