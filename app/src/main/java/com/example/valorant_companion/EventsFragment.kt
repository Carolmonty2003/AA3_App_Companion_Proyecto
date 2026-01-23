package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import RiotStatusApi.RiotStatusApiInstance
import RiotStatusApi.ValPlatformStatusResponse
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsFragment : Fragment(R.layout.fragment_events) {

    private val RIOT_API_KEY = "RGAPI-13cf5c83-a04b-4e20-bc77-f684d2d7e5a4"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = getString(R.string.EventsTitle)

        val recycler = view.findViewById<RecyclerView>(R.id.events_recycler)
        val splashLoading = view.findViewById<View>(R.id.events_loading_splash)
        val emptyCard = view.findViewById<View>(R.id.events_empty_card)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = StatusAdapter(emptyList()) { item ->

            val title = item.titles
                ?.firstOrNull { it.locale == "es-ES" && !it.title.isNullOrBlank() }
                ?.title
                ?: item.titles?.firstOrNull { !it.title.isNullOrBlank() }?.title
                ?: "Status"

            val type = if (!item.incident_severity.isNullOrBlank()) "incident" else "maintenance"
            val state = item.incident_severity ?: item.maintenance_status ?: "unknown"
            val createdAt = item.created_at ?: ""

            val lastUpdate = item.updates?.lastOrNull()
            val content = lastUpdate?.translations
                ?.firstOrNull { it.locale == "es-ES" && !it.content.isNullOrBlank() }
                ?.content
                ?: lastUpdate?.translations?.firstOrNull { !it.content.isNullOrBlank() }?.content
                ?: ""

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    StatusDetailFragment.newInstance(
                        title = title,
                        type = type,
                        state = state,
                        createdAt = createdAt,
                        content = content
                    )
                )
                .addToBackStack(null)
                .commit()
        }

        recycler.adapter = adapter

        // estado inicial
        splashLoading.visibility = View.VISIBLE
        recycler.visibility = View.GONE
        emptyCard.visibility = View.GONE

        RiotStatusApiInstance.api.getPlatformStatus(RIOT_API_KEY)
            .enqueue(object : Callback<ValPlatformStatusResponse> {

                override fun onResponse(
                    call: Call<ValPlatformStatusResponse>,
                    response: Response<ValPlatformStatusResponse>
                ) {
                    splashLoading.visibility = View.GONE

                    //para ver que si funciona la conexion a API
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Conectado a Riot API correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (!response.isSuccessful) {
                        emptyCard.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                        return
                    }

                    val body = response.body()
                    val items = (body?.incidents.orEmpty() + body?.maintenances.orEmpty())
                        .sortedByDescending { it.created_at ?: "" }

                    if (items.isEmpty()) {
                        emptyCard.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                    } else {
                        adapter.submit(items)
                        recycler.visibility = View.VISIBLE
                        emptyCard.visibility = View.GONE
                    }
                }


                override fun onFailure(call: Call<ValPlatformStatusResponse>, t: Throwable) {
                    splashLoading.visibility = View.GONE
                    emptyCard.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                }
            })
    }
}
