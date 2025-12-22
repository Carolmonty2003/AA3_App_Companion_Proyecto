package ValorantApi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton que centraliza la configuración de Retrofit para la API de Valorant.
 *
 * - Define la BASE_URL común.
 * - Construye una única instancia de Retrofit.
 * - Expone `api` (la interfaz ValorantApiService) para llamar a los endpoints.
 *
 */
object ValorantApiInstance {
    /** URL base del servidor (todas las llamadas se construyen a partir de aquí). */
    private const val BASE_URL = "https://valorant-api.com/v1/"

    /**
    * Servicio Retrofit listo para usar (ValorantApiService).
    *
    * `by lazy`:
    * - Crea el objeto solo la primera vez que se usa (no al arrancar la app).
    * - Evita recrearlo muchas veces.
    *
    * (IA): `by lazy`; esto es una mejora típica en Kotlin
    * para inicialización diferida.
    */
    val api: ValorantApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ValorantApiService::class.java)
    }
}