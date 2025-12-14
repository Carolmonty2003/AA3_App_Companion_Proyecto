package ValorantApi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ValorantApiService {
    @GET("v1/maps")
    fun getMaps(): Call<ValorantMapsResponse>
}