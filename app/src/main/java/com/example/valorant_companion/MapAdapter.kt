package com.example.valorant_companion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ValorantApi.ValorantMap
import com.example.valorant_companion.utils.SimpleImageLoader

class MapAdapter(
    private var items: List<ValorantMap>,
    private val onClick: (ValorantMap) -> Unit
) : RecyclerView.Adapter<MapAdapter.MapVH>() {

    fun submit(newItems: List<ValorantMap>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_map, parent, false)
        return MapVH(v, onClick)
    }

    override fun onBindViewHolder(holder: MapVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class MapVH(itemView: View, private val onClick: (ValorantMap) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val img: ImageView = itemView.findViewById(R.id.item_map_img)
        private val name: TextView = itemView.findViewById(R.id.item_map_name)

        fun bind(map: ValorantMap) {
            name.text = map.displayName ?: "Unknown"

            // listViewIcon = imagen de la lista
            SimpleImageLoader.load(
                map.listViewIcon,
                img,
                fallbackResId = R.drawable.ic_launcher_foreground
            )

            itemView.setOnClickListener { onClick(map) }
        }
    }
}
