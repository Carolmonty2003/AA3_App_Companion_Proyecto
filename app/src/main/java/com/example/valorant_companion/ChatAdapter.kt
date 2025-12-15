package com.example.valorant_companion

import ChatMessage
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val data: MutableList<ChatMessage>,
    private val myUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_ME = 1
        const val TYPE_OTHER = 2
    }

    fun addMessage(msg: ChatMessage) {
        data.add(msg)
        notifyItemInserted(data.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].uid == myUid) TYPE_ME else TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ME) {
            val v = inflater.inflate(R.layout.item_message_me, parent, false)
            MeVH(v)
        } else {
            val v = inflater.inflate(R.layout.item_message_other, parent, false)
            OtherVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val m = data[position]
        when (holder) {
            is MeVH -> holder.bind(m)
            is OtherVH -> holder.bind(m)
        }
    }

    override fun getItemCount(): Int = data.size

    class MeVH(v: View) : RecyclerView.ViewHolder(v) {
        private val user: TextView = v.findViewById(R.id.msg_user)
        private val text: TextView = v.findViewById(R.id.msg_text)

        fun bind(m: ChatMessage) {
            user.text = m.name ?: "You"
            text.text = m.text ?: ""
        }
    }

    class OtherVH(v: View) : RecyclerView.ViewHolder(v) {
        private val user: TextView = v.findViewById(R.id.msg_user)
        private val text: TextView = v.findViewById(R.id.msg_text)

        fun bind(m: ChatMessage) {
            user.text = m.name ?: "Player"
            text.text = m.text ?: ""
        }
    }
}

