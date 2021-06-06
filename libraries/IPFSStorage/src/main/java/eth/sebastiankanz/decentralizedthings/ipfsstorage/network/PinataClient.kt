package eth.sebastiankanz.decentralizedthings.ipfsstorage.network

import androidx.lifecycle.LiveData
import com.github.leonardoxh.livedatacalladapter.Resource
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

internal class PinataClient(
    private val pinataAPI: PinataAPI
) {
    companion object {
        private val gson = Gson()
    }

    suspend fun pinByHash(hash: String, pinName: String): PinataPinHashResponse {
        return pinataAPI.pinByHash(
            PinataPinRequest(
                hash,
                PinataMetaData(
                    pinName,
                    PinataKeyValue("Origin", "IPFS Android App")
                )
            )
        )
    }

    suspend fun unPinByHash(hash: String) = pinataAPI.unPin(hash)

    fun pinByFile(file: File, pinName: String): LiveData<Resource<PinataPinFileResponse>> {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val pinataMetaData = gson.toJson(
            PinataMetaData(
                pinName,
                PinataKeyValue(
                    "Origin",
                    "IPFS Android App"
                )
            )
        ).toRequestBody()
        val pinataOptions = gson.toJson(
            PinataOptions(
                PinataPinPolicy(listOf(PinataRegion("FRA1", 1))),
                0,
                false
            )
        ).toRequestBody()
        return pinataAPI.pinFileToIPFS(
            MultipartBody.Part.createFormData("file", file.name, requestFile),
            pinataMetaData,
            pinataOptions
        )
    }
}