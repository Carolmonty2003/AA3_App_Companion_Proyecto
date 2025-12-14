package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class MapDetailFragment : Fragment(R.layout.fragment_map_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = requireArguments().getString(ARG_NAME, "")
        val displayIcon = requireArguments().getString(ARG_DISPLAY_ICON, "")
        val splash = requireArguments().getString(ARG_SPLASH, "")

        view.findViewById<TextView>(R.id.map_name).text = name

        Glide.with(view).load(splash).into(view.findViewById<ImageView>(R.id.map_splash))
        Glide.with(view).load(displayIcon).into(view.findViewById<ImageView>(R.id.map_display_icon))
    }

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_DISPLAY_ICON = "displayIcon"
        private const val ARG_SPLASH = "splash"

        fun newInstance(name: String, displayIcon: String, splash: String) =
            MapDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putString(ARG_DISPLAY_ICON, displayIcon)
                    putString(ARG_SPLASH, splash)
                }
            }
    }
}
