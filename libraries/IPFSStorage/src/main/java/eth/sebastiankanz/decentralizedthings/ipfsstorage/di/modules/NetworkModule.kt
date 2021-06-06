package eth.sebastiankanz.decentralizedthings.ipfsstorage.di.modules

import com.github.leonardoxh.livedatacalladapter.LiveDataCallAdapterFactory
import com.github.leonardoxh.livedatacalladapter.LiveDataResponseBodyConverterFactory
import com.google.gson.GsonBuilder
import eth.sebastiankanz.decentralizedthings.ipfsstorage.network.PinataAPI
import eth.sebastiankanz.decentralizedthings.ipfsstorage.network.PinataClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

internal val networkModule = module {

    factory(named("pinataBaseURL")) {
        "https://api.pinata.cloud"
    }

    single(named("PinataAPI")) {
        createWebService<PinataAPI>(provideOkHttpClient(), get(named("pinataBaseURL")))
    }

    single<PinataClient>(named("PinataClient")) { PinataClient(get(named("PinataAPI"))) }

}

internal inline fun <reified T> createWebService(
    okHttpClient: OkHttpClient,
    baseUrl: String
): T {
    val gson = GsonBuilder().setLenient().create()
    val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(baseUrl)
        .addCallAdapterFactory(LiveDataCallAdapterFactory.create())
        .addConverterFactory(LiveDataResponseBodyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    return retrofit.create(T::class.java)
}

private fun provideOkHttpClient(): OkHttpClient {
    val builder = OkHttpClient.Builder()
    builder.addInterceptor(Interceptor { chain ->
        val request = chain.request().newBuilder()
        request.addHeader("Authorization", "Bearer ${PinataAPI.jwt}").build()
        chain.proceed(request.build())
    })
//    builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
    return builder.build()
}