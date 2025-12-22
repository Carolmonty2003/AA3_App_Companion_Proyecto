package ValorantApi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit que define los endpoints de la API.
 *
 * Retrofit genera automáticamente la implementación a partir de:
 * - @GET para indicar el endpoint
 * - @Query para query params (ej: ?language=es-ES)
 * - @Path para parámetros embebidos en la URL (ej: /agents/{uuid})
 *
 */
interface ValorantApiService {
    /**
     * Obtiene la lista de mapas.
     *
     * Endpoint real: GET /maps
     * @return Call con la respuesta tipada (ValorantMapsResponse)
     */
    @GET("maps")
    fun getMaps(): Call<ValorantMapsResponse>

//agentes
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