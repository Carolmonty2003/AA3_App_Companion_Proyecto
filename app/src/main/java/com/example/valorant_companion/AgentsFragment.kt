package com.example.valorant_companion

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ValorantApi.ValorantApiInstance
import ValorantApi.ValorantAgentsResponse
import com.google.firebase.analytics.FirebaseAnalytics
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgentsFragment : Fragment(R.layout.fragment_agents) {

    private var allAgents = emptyList<ValorantApi.ValorantAgent>()
    private var selectedRole: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val analytics = FirebaseAnalytics.getInstance(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.agents_recycler)
        val splashLoading = view.findViewById<View>(R.id.agents_loading_splash)

        // Imágenes barra roles
        val imgAll = view.findViewById<android.widget.ImageView>(R.id.imgAll)
        val imgDuelist = view.findViewById<android.widget.ImageView>(R.id.imgDuelist)
        val imgInitiator = view.findViewById<android.widget.ImageView>(R.id.imgInitiator)
        val imgController = view.findViewById<android.widget.ImageView>(R.id.imgController)
        val imgSentinel = view.findViewById<android.widget.ImageView>(R.id.imgSentinel)

        // Subrayados
        val underlineAll = view.findViewById<View>(R.id.underlineAll)
        val underlineDuelist = view.findViewById<View>(R.id.underlineDuelist)
        val underlineInitiator = view.findViewById<View>(R.id.underlineInitiator)
        val underlineController = view.findViewById<View>(R.id.underlineController)
        val underlineSentinel = view.findViewById<View>(R.id.underlineSentinel)



        recycler.layoutManager = GridLayoutManager(requireContext(), 4)

        val adapter = AgentAdapter(emptyList()) { agent ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AgentDetailFragment.newInstance(agent.uuid ?: ""))
                .addToBackStack(null)
                .commit()
        }

        recycler.adapter = adapter


        splashLoading.visibility = View.VISIBLE
        recycler.visibility = View.GONE

        val apiStartMs = SystemClock.elapsedRealtime()

        fun setUnderline(active: View) {
            val all = listOf(underlineAll, underlineDuelist, underlineInitiator, underlineController, underlineSentinel)
            all.forEach { it.setBackgroundColor(android.graphics.Color.TRANSPARENT) }
            active.setBackgroundColor(android.graphics.Color.RED)
        }

        fun roleMatches(agentRole: String?, selected: String?): Boolean {
            if (selected == null) return true
            // selected guarda "ES|EN"
            val parts = selected.split("|")
            return agentRole == parts[0] || agentRole == parts.getOrNull(1)
        }

        fun applyFilter() {
            val filtered = allAgents.filter { roleMatches(it.role?.displayName, selectedRole) }
            adapter.submit(filtered)
        }

        // Carga iconos desde la API
        fun loadRoleIcon(roleEs: String, roleEn: String, target: android.widget.ImageView) {
            val iconUrl = allAgents.firstOrNull {
                it.role?.displayName == roleEs || it.role?.displayName == roleEn
            }?.role?.displayIcon

            com.example.valorant_companion.utils.SimpleImageLoader.load(
                iconUrl,
                target,
                fallbackResId = R.drawable.ic_launcher_foreground
            )
        }

        // Estado inicial
        setUnderline(underlineAll)
        selectedRole = null

        view.findViewById<View>(R.id.btnAll).setOnClickListener {
            selectedRole = null
            setUnderline(underlineAll)
            applyFilter()
        }
        view.findViewById<View>(R.id.btnDuelist).setOnClickListener {
            selectedRole = "Duelista|Duelist"
            setUnderline(underlineDuelist)
            applyFilter()
        }
        view.findViewById<View>(R.id.btnInitiator).setOnClickListener {
            selectedRole = "Iniciador|Initiator"
            setUnderline(underlineInitiator)
            applyFilter()
        }
        view.findViewById<View>(R.id.btnController).setOnClickListener {
            selectedRole = "Controlador|Controller"
            setUnderline(underlineController)
            applyFilter()
        }
        view.findViewById<View>(R.id.btnSentinel).setOnClickListener {
            selectedRole = "Centinela|Sentinel"
            setUnderline(underlineSentinel)
            applyFilter()
        }


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

                    allAgents = agents

                    // Cargar iconos de la barra desde API
                    imgAll.setImageResource(R.drawable.ic_logo) // all = logo local
                    loadRoleIcon("Duelista", "Duelist", imgDuelist)
                    loadRoleIcon("Iniciador", "Initiator", imgInitiator)
                    loadRoleIcon("Controlador", "Controller", imgController)
                    loadRoleIcon("Centinela", "Sentinel", imgSentinel)

                    // Pintar lista (según filtro actual)
                    applyFilter()


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
