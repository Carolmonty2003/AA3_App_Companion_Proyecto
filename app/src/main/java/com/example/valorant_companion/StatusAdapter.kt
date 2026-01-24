package com.example.valorant_companion

import RiotStatusApi.ValStatusItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatusAdapter(
    private var items: List<ValStatusItem>,
    private val onClick: (ValStatusItem) -> Unit
) : RecyclerView.Adapter<StatusAdapter.StatusVH>() {

    fun submit(newItems: List<ValStatusItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_status, parent, false)
        return StatusVH(v)
    }

    override fun onBindViewHolder(holder: StatusVH, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount() = items.size

    class StatusVH(v: View) : RecyclerView.ViewHolder(v) {

        private val title: TextView = v.findViewById(R.id.status_title)
        private val meta: TextView = v.findViewById(R.id.status_meta)

        fun bind(item: ValStatusItem, onClick: (ValStatusItem) -> Unit) {
            // intenta sacar título en ES, si no, el primero
            val esTitle = item.titles?.firstOrNull { it.locale == "es-ES" }?.title
            val anyTitle = item.titles?.firstOrNull()?.title
            title.text = esTitle ?: anyTitle ?: "Status"

            val severity = item.incident_severity ?: item.maintenance_status ?: ""
            meta.text = severity

            itemView.setOnClickListener { onClick(item) }
        }
    }
}
