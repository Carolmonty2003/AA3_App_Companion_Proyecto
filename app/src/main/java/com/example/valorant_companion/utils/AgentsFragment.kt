package com.example.valorant_companion.utils

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ValorantApi.ValorantApiInstance
import ValorantApi.ValorantAgentsResponse
import com.example.valorant_companion.R
import com.google.firebase.analytics.FirebaseAnalytics
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgentsFragment : Fragment(R.layout.fragment_agents) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val analytics = FirebaseAnalytics.getInstance(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.agents_recycler)
        val splashLoading = view.findViewById<View>(R.id.agents_loading_splash)

        recycler.layoutManager = GridLayoutManager(requireContext(), 4)

        val adapter = AgentAdapter(emptyList()) { agent ->
            // aquí luego abrimos el detalle
        }
        recycler.adapter = adapter


        splashLoading.visibility = View.VISIBLE
        recycler.visibility = View.GONE

        val apiStartMs = SystemClock.elapsedRealtime()

        fun logAgentsLoad(success: Boolean, httpCode: Int, count: Int, durationMs: Long, errorMsg: String? = null) {
            val params = Bundle().apply {
                putLong("duration_ms", durationMs)
                putInt("http_code", httpCode)
                putInt("count", count)
                putBoolean("success", success)
                if (!errorMsg.isNullOrBlank()) putString("error", errorMsg.take(80))
            }
            analytics.logEvent("agents_load_time", params)
        }

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

        ValorantApiInstance.api.getAgents(isPlayable = true, language = "es-ES")
            .enqueue(object : Callback<ValorantAgentsResponse> {

                override fun onResponse(
                    call: Call<ValorantAgentsResponse>,
                    response: Response<ValorantAgentsResponse>
                ) {
                    val durationMs = SystemClock.elapsedRealtime() - apiStartMs

                    if (!response.isSuccessful) {
                        logAgentsLoad(false, response.code(), 0, durationMs, "HTTP_${response.code()}")
                        finishLoadingWithMin2s()
                        return
                    }

                    val agents = response.body()?.data
                        ?.filter { !it.displayName.isNullOrBlank() && !it.displayIcon.isNullOrBlank() }
                        ?: emptyList()

                    adapter.submit(agents)

                    logAgentsLoad(true, response.code(), agents.size, durationMs)
                    finishLoadingWithMin2s()
                }

                override fun onFailure(call: Call<ValorantAgentsResponse>, t: Throwable) {
                    val durationMs = SystemClock.elapsedRealtime() - apiStartMs
                    logAgentsLoad(false, -1, 0, durationMs, t.message ?: "failure")
                    finishLoadingWithMin2s()
                }
            })
    }
}
