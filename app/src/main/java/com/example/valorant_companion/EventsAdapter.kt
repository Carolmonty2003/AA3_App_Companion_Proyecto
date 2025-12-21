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

class EventsAdapter(
    private var items: List<ValorantEvent>,
    private val onClick: (ValorantEvent) -> Unit
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

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
                image.setImageDrawable(null) // MUY IMPORTANTE
                itemView.setBackgroundColor(
                    itemView.context.getColor(R.color.redPers)
                )

                // quitamos overlay oscuro
                name.setBackgroundColor(Color.TRANSPARENT)
            }

            itemView.setOnClickListener { onClick(event) }
        }
    }
}
