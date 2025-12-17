package ValorantApi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ValorantApiService {
    @GET("maps")
    fun getMaps(): Call<ValorantMapsResponse>


@GET("agents")
fun getAgents(
    @Query("isPlayableCharacter") isPlayable: Boolean = true,
    @Query("language") language: String = "es-ES"
): Call<ValorantAgentsResponse>

// ✅ NUEVO: detalle de un agente por uuid
@GET("agents/{uuid}")
fun getAgentByUuid(
    @Path("uuid") uuid: String,
    @Query("language") language: String = "es-ES"
): Call<ValorantAgentDetailResponse>

}