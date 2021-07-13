package eth.sebastiankanz.decentralizedthings.ipfsstorage.network

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

internal class InfuraClient(
    private val infuraAPI: InfuraAPI
) {
    companion object {
        private val gson = Gson()
    }

    suspend fun pinByFile(file: File): Response<InfuraPinFileResponse?> {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        return infuraAPI.pinFileToIPFS(
            MultipartBody.Part.createFormData("file", file.name, requestFile),
        )
    }

    suspend fun pinByHash(hash: String) = infuraAPI.pinByHash(hash)

    suspend fun unPinByHash(hash: String): Boolean {
        val unpinningResponse = infuraAPI.unPinByHash(hash)
        return unpinningResponse.pins.isNotEmpty()
    }
}