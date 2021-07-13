package eth.sebastiankanz.decentralizedthings.ipfsstorage.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

internal interface InfuraAPI {
    @Multipart
    @POST("/api/v0/add")
    suspend fun pinFileToIPFS(
        @Part file: MultipartBody.Part,
    ): Response<InfuraPinFileResponse?>

    @POST("/api/v0/pin/add")
    suspend fun pinByHash(
        @Query("arg") hash: String
    ): InfuraPinResponse


    @POST("/api/v0/pin/rm")
    suspend fun unPinByHash(
        @Query("arg") hash: String
    ): InfuraUnPinResponse
}

data class InfuraPinFileResponse constructor(
    @SerializedName("Name")
    val name: String,
    @SerializedName("Hash")
    val hash: String,
    @SerializedName("Size")
    val size: String
)

data class InfuraUnPinResponse constructor(
    @SerializedName("Pins")
    val pins: List<String>,
)

data class InfuraPinResponse constructor(
    @SerializedName("Pins")
    val pins: List<String>,
    @SerializedName("Progress")
    val progress: Int,
)