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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val analytics = FirebaseAnalytics.getInstance(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.maps_recycler)
        val splashLoading = view.findViewById<View>(R.id.maps_loading_splash)

        recycler.layoutManager = LinearLayoutManager(requireContext())

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

        // Splash visible al empezar
        splashLoading.visibility = View.VISIBLE
        recycler.visibility = View.GONE

        // --- MEDICIÓN TIEMPO API ---
        val apiStartMs = SystemClock.elapsedRealtime()

        fun logMapsLoad(success: Boolean, httpCode: Int, count: Int, durationMs: Long, errorMsg: String? = null) {
            val params = Bundle().apply {
                putLong("duration_ms", durationMs)
                putInt("http_code", httpCode)
                putInt("count", count)
                putBoolean("success", success)
                if (!errorMsg.isNullOrBlank()) putString("error", errorMsg.take(80))
            }
            analytics.logEvent("maps_load_time", params)
        }

        // si quieres mantener tu “mínimo 2s” de splash:
        val splashStartMs = SystemClock.elapsedRealtime()
        fun finishLoadingWithMin2s() {
            val elapsed = SystemClock.elapsedRealtime() - splashStartMs
            val delay = (2000L - elapsed).coerceAtLeast(0L)

            splashLoading.postDelayed({
                if (!isAdded) return@postDelayed
                splashLoading.visibility = View.GONE
                recycler.visibility = View.VISIBLE
            }, delay)
        }

        ValorantApiInstance.api.getMaps().enqueue(object : Callback<ValorantMapsResponse> {

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
