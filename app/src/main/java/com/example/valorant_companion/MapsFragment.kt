package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ValorantApi.ValorantApiInstance
import ValorantApi.ValorantMapsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsFragment : Fragment(R.layout.fragment_maps) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        val startMs = System.currentTimeMillis()
        splashLoading.visibility = View.VISIBLE
        recycler.visibility = View.GONE

        fun finishLoading() {
            val elapsed = System.currentTimeMillis() - startMs
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
                val maps = response.body()?.data
                    ?.filter { !it.displayName.isNullOrBlank() && !it.listViewIcon.isNullOrBlank() }
                    ?: emptyList()

                adapter.submit(maps)
                finishLoading()
            }

            override fun onFailure(call: Call<ValorantMapsResponse>, t: Throwable) {
                finishLoading()
            }
        })
    }
}
