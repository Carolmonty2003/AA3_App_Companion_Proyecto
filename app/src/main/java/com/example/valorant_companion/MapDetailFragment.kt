package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.valorant_companion.utils.SimpleImageLoader

class MapDetailFragment : Fragment(R.layout.fragment_map_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = requireArguments().getString(ARG_NAME, "")
        val displayIcon = requireArguments().getString(ARG_DISPLAY_ICON, "")
        val splash = requireArguments().getString(ARG_SPLASH, "")

        val loading = view.findViewById<View>(R.id.detail_loading_splash)
        loading.visibility = View.VISIBLE

        val splashIv = view.findViewById<ImageView>(R.id.map_splash)
        val iconIv = view.findViewById<ImageView>(R.id.map_display_icon)

        view.findViewById<TextView>(R.id.map_name).text = name

        var pending = 2
        fun doneOne() {
            pending--
            if (pending <= 0) loading.visibility = View.GONE
        }

        SimpleImageLoader.load(splash, splashIv, R.drawable.ic_launcher_foreground) { doneOne() }
        SimpleImageLoader.load(displayIcon, iconIv, R.drawable.ic_launcher_foreground) { doneOne() }
    }

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_DISPLAY_ICON = "displayIcon"
        private const val ARG_SPLASH = "splash"

        fun newInstance(displayName: String, displayIcon: String, splash: String) =
            MapDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, displayName)
                    putString(ARG_DISPLAY_ICON, displayIcon)
                    putString(ARG_SPLASH, splash)
                }
            }
    }
}
