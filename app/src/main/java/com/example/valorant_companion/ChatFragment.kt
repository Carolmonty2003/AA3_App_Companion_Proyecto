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

    /*
     * Se ejecuta cuando la vista del Fragment ya está creada.
     * Aquí se monta toda la pantalla de chat:
     * - RecyclerView + Adapter
     * - Conexión a Firebase Realtime Database (nodo "global_chat")
     * - Listener en tiempo real para leer los últimos 50 mensajes
     * - Envío de mensajes con push().key y setValue()
     *
     * @param {View} view - Vista raíz del Fragment.
     * @param {Bundle?} savedInstanceState - Estado guardado (si existe).
     * @returns {Unit} No devuelve nada.
     *
     * (IA): guardar query/listener para poder desmontarlo correctamente en onDestroyView,
     * y lógica “robusta” para sacar el nombre del usuario (custom register / Google / email).
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.chat_recycler)
        val input = view.findViewById<EditText>(R.id.chat_input)
        val send = view.findViewById<ImageButton>(R.id.chat_send)

        /*
         * Obtiene el UID del usuario autenticado.
         * Se usa para:
         * - Guardarlo en cada ChatMessage (campo uid)
         * - Compararlo en el Adapter y decidir si el mensaje es "mío" o "de otro"
         *
         * @returns {String} UID actual o "anon" si no hay sesión.
         *
         */
        val auth = FirebaseAuth.getInstance()
        val myUid = auth.currentUser?.uid ?: "anon"

        /*
         * Obtiene el nombre del usuario para mostrarlo en el chat.
         * Orden de prioridad:
         * 1) arguments (USER_NAME) -> lo pasas desde MainActivity como haces en Profile (custom register)
         * 2) displayName (si viene de Google)
         * 3) email (si no hay nombre guardado)
         * 4) GoogleSignIn last account displayName
         * 5) "Player" por defecto
         *
         * @returns {String} Nombre que se guardará en cada mensaje.
         *
         */
        val nameArg = arguments?.getString(ARG_USER_NAME)
        val googleAcc = GoogleSignIn.getLastSignedInAccount(requireContext())

        val myName = nameArg
            ?: auth.currentUser?.displayName
            ?: auth.currentUser?.email
            ?: googleAcc?.displayName
            ?: "Player"

        adapter = ChatAdapter(messages, myUid)

        /*
         * LayoutManager:
         * - stackFromEnd = true hace que la lista “arranque” desde abajo (como un chat)
         */
        recycler.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        recycler.adapter = adapter

        /*
         * Conexión a Firebase Realtime Database.
         * Usas URL explícita para asegurar región/instancia correcta.
         *
         * @returns {DatabaseReference} referencia al nodo "global_chat".
         *
         */
        val databaseUrl = "https://aa3appcompanionvalorant-default-rtdb.europe-west1.firebasedatabase.app/"
        val database = FirebaseDatabase.getInstance(databaseUrl)
        dbRef = database.getReference("global_chat")

        // Limpia la lista antes de empezar a escuchar, para evitar duplicados si el Fragment se reconstruye.
        messages.clear()
        adapter.notifyDataSetChanged()

        //Crea una Query para traer solo los últimos 50 mensajes ordenados por timestamp.
        query = dbRef.orderByChild("timestamp").limitToLast(50)

        /*
         * Listener en tiempo real:
         * - onChildAdded se dispara por cada mensaje existente (últimos 50) y por cada nuevo mensaje.
         * - Se convierte el snapshot a ChatMessage y se añade al adapter.
         * - scrollToPosition para mantener el chat “abajo” cuando entra un mensaje.
         *
         * (IA): scrollToPosition constante para simular comportamiento de chat.
         */
        listener = query!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(ChatMessage::class.java) ?: return
                adapter.addMessage(msg)
                recycler.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            /*
             * Si Firebase cancela el listener (reglas, permisos, error de conexión…)
             * se muestra un Toast para debug.
             */
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "DB ERROR: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })

        /*
         * Botón enviar:
         * - Lee texto del EditText
         * - Crea ChatMessage con uid, name, text y timestamp
         * - Genera id con push().key
         * - Guarda en dbRef.child(id).setValue(msg)
         * - Limpia el input
         */
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

    /*
     * Se ejecuta cuando la vista del Fragment se destruye.
     * Aquí quitamos el listener para:
     * - evitar fugas de memoria
     * - evitar duplicar listeners al volver a entrar al chat
     *
     * @returns {Unit} No devuelve nada.
     *
     * (IA): guardar query/listener como propiedades y desmontarlos aquí.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        listener?.let { l -> query?.removeEventListener(l) }
        listener = null
        query = null
    }

    companion object {
        private const val ARG_USER_NAME = "USER_NAME"

        /*
         * Crea una instancia del ChatFragment y le pasa el nombre por arguments.
         * Esto es útil para tu custom register, donde no se guarda displayName en FirebaseAuth.
         *
         * @param {String?} userName - Nombre que quieres usar en el chat.
         * @returns {ChatFragment} Fragment listo con arguments.
         *
         */
        fun newInstance(userName: String?) = ChatFragment().apply {
            arguments = Bundle().apply { putString(ARG_USER_NAME, userName) }
        }
    }
}
