package com.example.valorant_companion

import ValorantApi.ValorantEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        val event = items[position]

        holder.name.text = event.displayName ?: ""
        holder.image.setImageResource(R.drawable.ic_logo)

        holder.itemView.setOnClickListener {
            onClick(event)
        }
    }

    override fun getItemCount(): Int = items.size

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.item_event_img)
        val name: TextView = view.findViewById(R.id.item_event_name)
    }
}
