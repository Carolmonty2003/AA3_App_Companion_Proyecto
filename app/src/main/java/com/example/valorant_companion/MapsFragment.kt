package com.example.valorant_companion

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ValorantApi.ValorantApiInstance
import ValorantApi.ValorantMapsResponse
import com.google.firebase.analytics.FirebaseAnalytics
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsFragment : Fragment(R.layout.fragment_maps) {

    /**
     * Inicializa la pantalla de Maps:
     * - Configura el RecyclerView y su Adapter.
     * - Muestra una "splash" de carga mientras se trae la lista de mapas desde la API.
     * - Lanza la llamada Retrofit (enqueue) y cuando llega:
     *   - Filtra datos válidos
     *   - Pinta la lista
     *   - Navega a MapDetailFragment al hacer click en un item
     * - Registra un evento de Firebase Analytics con el tiempo de carga de la API.
     *
     * (IA) El mínimo 2seg de splash y el log del tiempo con SystemClock + Analytics, y truncar errorMsg.
     *
     * @param {View} view - Vista raíz del fragment ya inflada.
     * @param {Bundle?} savedInstanceState - Estado guardado (si existe).
     * @returns {Unit}
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (Apuntes) Analytics: obtener instancia para registrar eventos
        val analytics = FirebaseAnalytics.getInstance(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.maps_recycler)
        val splashLoading = view.findViewById<View>(R.id.maps_loading_splash)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        /**
         * Adapter del RecyclerView.
         * onClick: cuando se pulsa un mapa, se reemplaza el fragment actual por MapDetailFragment.
         *
         * (IA) Usar parentFragmentManager directamente con replace al contenedor:
         * es correcto, pero la “forma exacta” depende de cómo lo haya hecho el profe.
         */
        val adapter = MapAdapter(emptyList()) { map ->
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    MapDetailFragment.newInstance(
                        displayName = map.displayName ?: "",
                        displayIcon = map.displayIcon ?: "",
                        splash = map.splash ?: ""
                    )
                )
                .addToBackStack(null)
                .commit()
        }
        recycler.adapter = adapter

        // Splash visible al empezar (mientras no hay datos)
        splashLoading.visibility = View.VISIBLE
        recycler.visibility = View.GONE

        // --- MEDICIÓN TIEMPO API ---
        //(IA): SystemClock.elapsedRealtime() es buena práctica para medir duraciones reales.
        val apiStartMs = SystemClock.elapsedRealtime()

        /**
         * Registra en Firebase Analytics un evento con información de carga.
         * Guarda:
         * - duration_ms: cuánto tardó la petición
         * - http_code: código HTTP (o -1 en fallo)
         * - count: cuántos mapas llegaron
         * - success: true/false
         * - error: texto corto de error
         *
         * (IA) cortar el mensaje error a 80 chars para no mandar strings enormes.
         *
         * @param {Boolean} success - Si fue OK o error.
         * @param {Int} httpCode - Código HTTP o -1 si es fallo de red.
         * @param {Int} count - Nº de mapas cargados.
         * @param {Long} durationMs - Duración total en ms.
         * @param {String?} errorMsg - Mensaje opcional.
         * @returns {Unit}
         */
        fun logMapsLoad(
            success: Boolean,
            httpCode: Int,
            count: Int,
            durationMs: Long,
            errorMsg: String? = null
        ) {
            val params = Bundle().apply {
                putLong("duration_ms", durationMs)
                putInt("http_code", httpCode)
                putInt("count", count)
                putBoolean("success", success)
                if (!errorMsg.isNullOrBlank()) putString("error", errorMsg.take(80))
            }
            analytics.logEvent("maps_load_time", params)
        }

        /**
         * Mantiene la splash screen un mínimo de 2 segundos.
         * Si la API responde antes, espera el tiempo restante.
         * Si la API tarda más, no añade delay extra.
         *
         * (IA): este “mínimo 2s” NO es algo típico de apuntes, es una mejora estética.
         *
         * @returns {Unit}
         */
        val splashStartMs = SystemClock.elapsedRealtime()
        fun finishLoadingWithMin2s() {
            val elapsed = SystemClock.elapsedRealtime() - splashStartMs
            val delay = (2000L - elapsed).coerceAtLeast(0L)

            splashLoading.postDelayed({
                // Evita crasheos si el fragment ya no está “attached”
                if (!isAdded) return@postDelayed
                splashLoading.visibility = View.GONE
                recycler.visibility = View.VISIBLE
            }, delay)
        }

        //Llamada a la API con Retrofit (asíncrona).
        ValorantApiInstance.api.getMaps().enqueue(object : Callback<ValorantMapsResponse> {

            /**
             * Respuesta correcta del servidor (puede ser successful o no).
             * - Si response.isSuccessful == false -> log + terminar loading
             * - Si OK -> filtra mapas con displayName y listViewIcon -> adapter.submit()
             *
             */
            override fun onResponse(
                call: Call<ValorantMapsResponse>,
                response: Response<ValorantMapsResponse>
            ) {
                val durationMs = SystemClock.elapsedRealtime() - apiStartMs

                if (!response.isSuccessful) {
                    logMapsLoad(
                        success = false,
                        httpCode = response.code(),
                        count = 0,
                        durationMs = durationMs,
                        errorMsg = "HTTP_${response.code()}"
                    )
                    finishLoadingWithMin2s()
                    return
                }

                // Filtras para evitar items sin nombre o sin icono de lista.
                val maps = response.body()?.data
                    ?.filter { !it.displayName.isNullOrBlank() && !it.listViewIcon.isNullOrBlank() }
                    ?: emptyList()

                adapter.submit(maps)

                logMapsLoad(
                    success = true,
                    httpCode = response.code(),
                    count = maps.size,
                    durationMs = durationMs
                )

                finishLoadingWithMin2s()
            }

            //Fallo de red / DNS / sin internet / timeout, etc.
            override fun onFailure(call: Call<ValorantMapsResponse>, t: Throwable) {
                val durationMs = SystemClock.elapsedRealtime() - apiStartMs

                logMapsLoad(
                    success = false,
                    httpCode = -1,
                    count = 0,
                    durationMs = durationMs,
                    errorMsg = t.message ?: "failure"
                )

                finishLoadingWithMin2s()
            }
        })
    }
}
