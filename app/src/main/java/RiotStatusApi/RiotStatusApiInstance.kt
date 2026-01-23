// RiotStatusApiInstance.kt
package RiotStatusApi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RiotStatusApiInstance {

    private const val BASE_URL = "https://eu.api.riotgames.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: RiotStatusApiService by lazy {
        retrofit.create(RiotStatusApiService::class.java)
    }
}
