package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.IPFSObjectType
import eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model.SyncState
import kotlinx.serialization.json.Json

internal class IPFSObjectConverters {

    private val gson = Gson()

    @TypeConverter
    fun toSyncState(state: Int): SyncState? {
        return when (state) {
            SyncState.NONE.state -> {
                SyncState.NONE
            }
            SyncState.SYNCED.state -> {
                SyncState.SYNCED
            }
            SyncState.UNSYNCED_ONLY_LOCAL.state -> {
                SyncState.UNSYNCED_ONLY_LOCAL
            }
            SyncState.UNSYNCED_ONLY_REMOTE.state -> {
                SyncState.UNSYNCED_ONLY_REMOTE
            }
            SyncState.UNSYNCED_ONLY_PARTLY.state -> {
                SyncState.UNSYNCED_ONLY_PARTLY
            }
            else -> {
                throw IllegalArgumentException("Could not recognize status")
            }
        }
    }

    @TypeConverter
    fun toString(type: IPFSObjectType): String {
        return Json.encodeToString(IPFSObjectType.serializer(), type)
    }

    @TypeConverter
    fun toIPFSObjectType(type: String): IPFSObjectType {
        return Json.decodeFromString(IPFSObjectType.serializer(), type)
    }

    @TypeConverter
    fun toInteger(syncState: SyncState): Int {
        return syncState.state
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