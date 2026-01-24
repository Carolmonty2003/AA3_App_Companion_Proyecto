// RiotStatusApiService.kt
package RiotStatusApi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface RiotStatusApiService {
    @GET("val/status/v1/platform-data")
    fun getPlatformStatus(
        @Header("X-Riot-Token") apiKey: String
    ): Call<ValPlatformStatusResponse>
}
