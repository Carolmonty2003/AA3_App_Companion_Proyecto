package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
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
        val loading = view.findViewById<ProgressBar>(R.id.maps_loading)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = MapAdapter(emptyList()) { map ->
            // abrir plantilla reutilizable de detalle
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    MapDetailFragment.newInstance(
                        map.displayName ?: "",
                        map.displayIcon ?: "",
                        map.splash ?: ""
                    )
                )
                .addToBackStack(null)
                .commit()
        }
        recycler.adapter = adapter

        loading.visibility = View.VISIBLE
        recycler.visibility = View.GONE

        ValorantApiInstance.api.getMaps().enqueue(object : Callback<ValorantMapsResponse> {
            override fun onResponse(
                call: Call<ValorantMapsResponse>,
                response: Response<ValorantMapsResponse>
            ) {
                val maps = response.body()?.data
                    ?.filter { !it.displayName.isNullOrBlank() && !it.listViewIcon.isNullOrBlank() }
                    ?: emptyList()

                adapter.submit(maps)

                loading.visibility = View.GONE
                recycler.visibility = View.VISIBLE
            }

            override fun onFailure(call: Call<ValorantMapsResponse>, t: Throwable) {
                loading.visibility = View.GONE
                // aquí luego puedes poner un TextView de error si quieres
                recycler.visibility = View.VISIBLE
            }
        })
    }
}
