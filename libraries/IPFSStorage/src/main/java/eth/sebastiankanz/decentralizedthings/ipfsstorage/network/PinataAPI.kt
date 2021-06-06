package eth.sebastiankanz.decentralizedthings.ipfsstorage.network

import androidx.lifecycle.LiveData
import com.github.leonardoxh.livedatacalladapter.Resource
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

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

    companion object {
        const val jwt =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mb3JtYXRpb24iOnsiaWQiOiJlOWM4OTQyMi1mNjJhLTRiYzEtODkxMC00MWEyZWU1MTZjYTAiLCJlbWFpbCI6InNlYmFzdGlhbi5rYW56QGdvb2dsZW1haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsInBpbl9wb2xpY3kiOnsicmVnaW9ucyI6W3siaWQiOiJGUkExIiwiZGVzaXJlZFJlcGxpY2F0aW9uQ291bnQiOjF9XSwidmVyc2lvbiI6MX0sIm1mYV9lbmFibGVkIjpmYWxzZX0sImF1dGhlbnRpY2F0aW9uVHlwZSI6InNjb3BlZEtleSIsInNjb3BlZEtleUtleSI6IjA4Mzc2YzlmYzA0MjNjNmE1Y2QzIiwic2NvcGVkS2V5U2VjcmV0IjoiYWQ1YTMyY2FkM2E1OWQzYTQyYjAwZjQ0NWFmYjE1MTg1MmNmNzEyY2QyMTg1OGJmZjYyODgzMDk2NTQ3ZDg0ZiIsImlhdCI6MTYwODU3NjA5OH0.LB3prOCOytmEtou_qQg9JrgQVcLLr7nt3SWmOaR-t-g"
        const val apiSecret = "ad5a32cad3a59d3a42b00f445afb151852cf712cd21858bff62883096547d84f"
        const val apiKey = "08376c9fc0423c6a5cd3"
    }
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
