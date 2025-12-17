package com.example.valorant_companion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val myUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ME = 1
        private const val TYPE_OTHER = 2
    }

    override fun getItemViewType(position: Int): Int {
        val msgUid = messages[position].uid
        return if (!msgUid.isNullOrBlank() && msgUid == myUid) TYPE_ME else TYPE_OTHER
    }

    class MeVH(v: View) : RecyclerView.ViewHolder(v) {
        val user: TextView = v.findViewById(R.id.msg_user)
        val text: TextView = v.findViewById(R.id.msg_text)
    }

    class OtherVH(v: View) : RecyclerView.ViewHolder(v) {
        val user: TextView = v.findViewById(R.id.msg_user)
        val text: TextView = v.findViewById(R.id.msg_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ME) {
            MeVH(inf.inflate(R.layout.item_message_me, parent, false))
        } else {
            OtherVH(inf.inflate(R.layout.item_message_other, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val m = messages[position]
        val name = m.name ?: "Player"
        val text = m.text ?: ""

        when (holder) {
            is MeVH -> {
                holder.user.text = name
                holder.text.text = text
            }
            is OtherVH -> {
                holder.user.text = name
                holder.text.text = text
            }
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }
}
