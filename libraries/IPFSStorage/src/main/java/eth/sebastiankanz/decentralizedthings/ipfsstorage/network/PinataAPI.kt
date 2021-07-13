package eth.sebastiankanz.decentralizedthings.ipfsstorage.network

import androidx.lifecycle.LiveData
import com.github.leonardoxh.livedatacalladapter.Resource
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

internal interface PinataAPI {
    @Headers("Content-Type: application/json")
    @POST("pinning/pinByHash")
    suspend fun pinByHash(
        @Body request: PinataPinRequest
    ): PinataPinHashResponse

    @Multipart
    @POST("pinning/pinFileToIPFS")
    fun pinFileToIPFS(
        @Part file: MultipartBody.Part,
        @Part("pinataMetadata") pinataMetadata: RequestBody,
        @Part("pinataOptions") pinataOptions: RequestBody
    ): LiveData<Resource<PinataPinFileResponse>>

    @DELETE("pinning/unpin/{hash}")
    suspend fun unPin(
        @Path("hash") hash: String
    ): ResponseBody

    @GET("pinning/pinJobs")
    suspend fun getActivePinningJobForHash(
        @Query("ipfs_pin_hash") hash: String
    ): PinataPinningJobResponse
}

data class PinataPinHashResponse constructor(
    @SerializedName("id")
    val id: String,
    @SerializedName("ipfsHash")
    val ipfsHash: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("name")
    val name: String
)

data class PinataPinFileResponse constructor(
    @SerializedName("IpfsHash")
    val ipfsHash: String,
    @SerializedName("PinSize")
    val pinSize: String,
    @SerializedName("Timestamp")
    val timestamp: String
)

data class PinataPinRequest constructor(
    @SerializedName("hashToPin")
    val hashToPin: String,
    @SerializedName("pinataMetadata")
    val pinataMetadata: PinataMetaData
)

data class PinataMetaData constructor(
    @SerializedName("name")
    val name: String,
    @SerializedName("keyvalues")
    val keyvalues: PinataKeyValue,
)

data class PinataKeyValue constructor(
    @SerializedName("key")
    val key: String,
    @SerializedName("name")
    val name: String,
)

data class PinataOptions constructor(
    @SerializedName("customPinPolicy")
    val customPinPolicy: PinataPinPolicy,
    @SerializedName("cidVersion")
    val cidVersion: Int,
    @SerializedName("wrapWithDirectory")
    val wrapWithDirectory: Boolean,
)

data class PinataPinPolicy constructor(
    @SerializedName("regions")
    val regions: List<PinataRegion>,
)

data class PinataRegion constructor(
    @SerializedName("id")
    val id: String,
    @SerializedName("desiredReplicationCount")
    val desiredReplicationCount: Int,
)

data class PinataPinningJobResponse constructor(
    @SerializedName("count")
    val count: Int,
    @Transient
    val rows: String,
)