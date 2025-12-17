package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var dbRef: DatabaseReference
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    private var query: Query? = null
    private var listener: ChildEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.chat_recycler)
        val input = view.findViewById<EditText>(R.id.chat_input)
        val send = view.findViewById<ImageButton>(R.id.chat_send)

        // --- UID + nombre (misma idea que ProfileFragment para custom register) ---
        val auth = FirebaseAuth.getInstance()
        val myUid = auth.currentUser?.uid ?: "anon"

        val nameArg = arguments?.getString(ARG_USER_NAME)
        val googleAcc = GoogleSignIn.getLastSignedInAccount(requireContext())

        val myName = nameArg
            ?: auth.currentUser?.displayName
            ?: auth.currentUser?.email
            ?: googleAcc?.displayName
            ?: "Player"

        adapter = ChatAdapter(messages, myUid)

        recycler.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        recycler.adapter = adapter

        // --- Realtime Database ---
        val databaseUrl = "https://aa3appcompanionvalorant-default-rtdb.europe-west1.firebasedatabase.app/"
        val database = FirebaseDatabase.getInstance(databaseUrl)
        dbRef = database.getReference("global_chat")

        messages.clear()
        adapter.notifyDataSetChanged()

        // --- últimos 50 ---
        query = dbRef.orderByChild("timestamp").limitToLast(50)

        listener = query!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(ChatMessage::class.java) ?: return
                adapter.addMessage(msg)
                recycler.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "DB ERROR: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })

        // --- enviar ---
        send.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isBlank()) return@setOnClickListener

            val msg = ChatMessage(
                uid = myUid,
                name = myName,
                text = text,
                timestamp = System.currentTimeMillis()
            )

            val id = dbRef.push().key ?: return@setOnClickListener
            dbRef.child(id).setValue(msg)
            input.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.let { l -> query?.removeEventListener(l) }
        listener = null
        query = null
    }

    companion object {
        private const val ARG_USER_NAME = "USER_NAME"

        fun newInstance(userName: String?) = ChatFragment().apply {
            arguments = Bundle().apply { putString(ARG_USER_NAME, userName) }
        }
    }
}
