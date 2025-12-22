package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.valorant_companion.utils.SimpleImageLoader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// fragment que muestra el detalle de un evento
class EventDetailFragment : Fragment(R.layout.fragment_events_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = requireArguments().getString(ARG_NAME, "")
        val icon = requireArguments().getString(ARG_ICON, "")

        val start = requireArguments().getString(ARG_START)
        val end = requireArguments().getString(ARG_END)

        // ocultamos splash de carga
        val loading = view.findViewById<View>(R.id.detail_loading_splash)
        loading.visibility = View.GONE

        //nombre evento
        view.findViewById<TextView>(R.id.event_name).text = name

        val image = view.findViewById<ImageView>(R.id.event_display_icon)

        // Si el evento tiene imagen, la cargamos desde la URL, si no, mostramos un icono por defecto
        if (!icon.isNullOrBlank()) {
            SimpleImageLoader.load(
                icon,
                image,
                R.drawable.ic_logo   // imagen de respaldo si falla la carga
            )
        } else {
            image.setImageResource(R.drawable.ic_logo)
        }

        // mostramos las fechas del evento
        val datesTv = view.findViewById<TextView>(R.id.event_dates)
        datesTv.text = formatDates(start, end)
    }

    // Convierte las fechas de la API
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
        // argumentos para pasar datos entre fragments mediante Bundle
        private const val ARG_NAME = "name"
        private const val ARG_ICON = "icon"
        private const val ARG_EVENT_ID = "event_id"
        private const val ARG_START = "start"
        private const val ARG_END = "end"

        //crear fragments con argumentos
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