package ValorantApi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ValorantApiInstance {
    private const val BASE_URL = "https://valorant-api.com/v1/"

    val api: ValorantApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ValorantApiService::class.java)
    }
}