package com.example.valorant_companion.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ValorantApi.ValorantAgent
import com.example.valorant_companion.R
import com.example.valorant_companion.utils.SimpleImageLoader

class AgentAdapter(
    private var items: List<ValorantAgent>,
    private val onClick: (ValorantAgent) -> Unit
) : RecyclerView.Adapter<AgentAdapter.AgentVH>() {

    fun submit(newItems: List<ValorantAgent>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgentVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_agent, parent, false)
        return AgentVH(v, onClick)
    }

    override fun onBindViewHolder(holder: AgentVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class AgentVH(itemView: View, private val onClick: (ValorantAgent) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val img: ImageView = itemView.findViewById(R.id.item_agent_img)

        fun bind(agent: ValorantAgent) {
            SimpleImageLoader.load(
                agent.displayIcon,
                img,
                fallbackResId = R.drawable.ic_launcher_foreground
            )
            itemView.setOnClickListener { onClick(agent) }
        }
    }
}
