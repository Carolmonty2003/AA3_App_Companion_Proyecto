package com.example.valorant_companion

import ValorantApi.ValorantEvent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.valorant_companion.utils.SimpleImageLoader

// adapter encargado de gestionar la lista de eventos en el RecyclerView
class EventsAdapter(
    private var items: List<ValorantEvent>,
    private val onClick: (ValorantEvent) -> Unit
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    // actualiza la lista de eventos y refresca la vista
    fun submit(newItems: List<ValorantEvent>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)

        return EventViewHolder(view)
    }
    // asocia los datos de un evento con su ViewHolder
    override fun onBindViewHolder(
        holder: EventViewHolder,
        position: Int
    ) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val image: ImageView = view.findViewById(R.id.item_event_img)
        private val name: TextView = view.findViewById(R.id.item_event_name)

        // rellena el item con los datos del evento
        fun bind(event: ValorantEvent, onClick: (ValorantEvent) -> Unit) {
            name.text = event.displayName ?: "Unknown"

            if (!event.displayIcon.isNullOrBlank()) {
                // Hay imagen
                SimpleImageLoader.load(
                    event.displayIcon,
                    image,
                    R.drawable.ic_logo
                )

                name.setBackgroundColor(0x66000000)
                itemView.setBackgroundColor(Color.TRANSPARENT)

            } else {
                // No hay imagen
                image.setImageDrawable(null)
                itemView.setBackgroundColor(
                    itemView.context.getColor(R.color.redPers)
                )

                // quitamos overlay oscuro
                name.setBackgroundColor(Color.TRANSPARENT)
            }

            // listener de click para navegar al detalle del evento
            itemView.setOnClickListener { onClick(event) }
        }
    }
}
