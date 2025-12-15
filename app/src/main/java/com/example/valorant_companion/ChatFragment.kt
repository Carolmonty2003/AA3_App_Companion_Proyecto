package com.example.valorant_companion

import ChatMessage
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var baseRef: DatabaseReference
    private var query: Query? = null
    private var listener: ChildEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val myUid = auth.currentUser?.uid ?: "anon"
        val myName = auth.currentUser?.displayName
            ?: auth.currentUser?.email
            ?: "Player"

        baseRef = FirebaseDatabase.getInstance().reference.child("global_chat")

        val recycler = view.findViewById<RecyclerView>(R.id.chat_recycler)
        val input = view.findViewById<EditText>(R.id.chat_input)
        val sendBtn = view.findViewById<View>(R.id.chat_send) // sirve si es Button o ImageView

        adapter = ChatAdapter(messages, myUid)

        recycler.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        recycler.adapter = adapter

        // --- LISTENER realtime (últimos 50 mensajes) ---
        query = baseRef.orderByChild("timestamp").limitToLast(50)

        listener = query!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(ChatMessage::class.java) ?: return
                adapter.addMessage(msg)
                recycler.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        // --- ENVIAR mensaje (push key + map + setValue) ---
        sendBtn.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isBlank()) return@setOnClickListener

            val id = baseRef.push().key ?: return@setOnClickListener

            val map = hashMapOf<String, Any>(
                "uid" to myUid,
                "name" to myName,
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            )

            baseRef.child(id).setValue(map)
            input.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.let { l ->
            query?.removeEventListener(l)
        }
        listener = null
        query = null
    }
}
