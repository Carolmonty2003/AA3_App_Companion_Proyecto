package com.example.valorant_companion

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ValorantApi.ValorantApiInstance
import ValorantApi.ValorantEventsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//muestra la lista de eventos
class EventsFragment : Fragment(R.layout.fragment_events) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = getString(R.string.EventsTitle)

        val recycler = view.findViewById<RecyclerView>(R.id.events_recycler)
        val splashLoading = view.findViewById<View>(R.id.events_loading_splash)

        // RecyclerView con layout vertical
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // adapter con callback para ir al detalle del evento
        val adapter = EventsAdapter(emptyList()) { event ->
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    EventDetailFragment.newInstance(
                            eventId = event.uuid ?: "",
                            displayName = event.displayName ?: "",
                            displayIcon = event.displayIcon ?: "",
                            startTime = event.startTime,
                            endTime = event.endTime
                        )
                )
                .addToBackStack(null)
                .commit()
        }
        recycler.adapter = adapter

        // splash al empezar
        splashLoading.visibility = View.VISIBLE
        recycler.visibility = View.GONE

        // medición del tiempo de llamada a la API
        val apiStartMs = SystemClock.elapsedRealtime()

        // llamada a API events
        ValorantApiInstance.api.getEvents().enqueue(object : Callback<ValorantEventsResponse> {

            override fun onResponse(
                call: Call<ValorantEventsResponse>,
                response: Response<ValorantEventsResponse>
            ) {
                val durationMs = SystemClock.elapsedRealtime() - apiStartMs

                if (!response.isSuccessful) {
                    splashLoading.visibility = View.GONE
                    return
                }

                val events = response.body()?.data
                    ?.filter {
                        !it.displayName.isNullOrBlank()
                    }
                    ?: emptyList()

                // actualizamos la lista
                adapter.submit(events)

                splashLoading.visibility = View.GONE
                recycler.visibility = View.VISIBLE
            }

            override fun onFailure(call: Call<ValorantEventsResponse>, t: Throwable) {
                splashLoading.visibility = View.GONE
            }
        })
    }
}