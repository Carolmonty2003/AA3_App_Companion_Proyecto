package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class EventDetailFragment : Fragment(R.layout.fragment_events_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = requireArguments().getString(ARG_NAME, "")
        val icon = requireArguments().getString(ARG_ICON, "")

        val loading = view.findViewById<View>(R.id.detail_loading_splash)
        loading.visibility = View.GONE

        view.findViewById<TextView>(R.id.event_name).text = name

        val image = view.findViewById<ImageView>(R.id.event_display_icon)
        image.setImageResource(R.drawable.ic_logo)
    }

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_ICON = "icon"
        private const val ARG_EVENT_ID = "event_id"

        fun newInstance(
            eventId: String,
            displayName: String,
            displayIcon: String
        ) = EventDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_EVENT_ID, eventId)
                putString(ARG_NAME, displayName)
                putString(ARG_ICON, displayIcon)
            }
        }
    }
}