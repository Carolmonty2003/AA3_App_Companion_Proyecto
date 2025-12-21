package com.example.valorant_companion

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.valorant_companion.utils.SimpleImageLoader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventDetailFragment : Fragment(R.layout.fragment_events_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = requireArguments().getString(ARG_NAME, "")
        val icon = requireArguments().getString(ARG_ICON, "")

        Log.d("EVENT_ICON", icon ?: "NO IMAGE")

        val start = requireArguments().getString(ARG_START)
        val end = requireArguments().getString(ARG_END)

        val loading = view.findViewById<View>(R.id.detail_loading_splash)
        loading.visibility = View.GONE

        view.findViewById<TextView>(R.id.event_name).text = name

        val image = view.findViewById<ImageView>(R.id.event_display_icon)

        if (!icon.isNullOrBlank()) {
            SimpleImageLoader.load(
                icon,
                image,
                R.drawable.ic_logo   // fallback si falla
            )
        } else {
            image.setImageResource(R.drawable.ic_logo)
        }

        val datesTv = view.findViewById<TextView>(R.id.event_dates)
        datesTv.text = formatDates(start, end)
    }

    private fun formatDates(start: String?, end: String?): String {
        if (start.isNullOrBlank() || end.isNullOrBlank()) return ""

        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            val startDate: Date = parser.parse(start) ?: return ""
            val endDate: Date = parser.parse(end) ?: return ""

            "${formatter.format(startDate)} – ${formatter.format(endDate)}"
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_ICON = "icon"
        private const val ARG_EVENT_ID = "event_id"
        private const val ARG_START = "start"
        private const val ARG_END = "end"

        fun newInstance(
            eventId: String,
            displayName: String,
            displayIcon: String,
            startTime: String?,
            endTime: String?
        ) = EventDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_EVENT_ID, eventId)
                putString(ARG_NAME, displayName)
                putString(ARG_ICON, displayIcon)
                putString(ARG_START, startTime)
                putString(ARG_END, endTime)
            }
        }
    }
}