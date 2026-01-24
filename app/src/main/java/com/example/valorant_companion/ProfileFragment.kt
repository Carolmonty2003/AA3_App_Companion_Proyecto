package com.example.valorant_companion

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var userName: TextView
    private lateinit var profileImage: ImageView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var userRef: DatabaseReference

    /*
     * Inicializa la pantalla de Profile:
     * - Conecta las vistas (TextView e ImageView).
     * - Inicializa FirebaseAuth y GoogleSignInClient.
     * - Decide el nombre e imagen final a mostrar combinando:
     *   arguments -> firebaseUser -> googleAccount -> fallback.
     * - Carga la imagen según el tipo de URL (content/file o http).
     *
     * @param {View} view - Vista raíz del fragment ya inflada.
     * @param {Bundle?} savedInstanceState - Estado guardado (rotación, recreación).
     * @returns {Unit} No devuelve nada.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Conectar vistas (sin “sombras”)
        userName = view.findViewById(R.id.userName)
        val btnEdit = view.findViewById<ImageButton>(R.id.btn_edit_name)
        val layoutEdit = view.findViewById<View>(R.id.layout_edit_name)
        val input = view.findViewById<EditText>(R.id.input_edit_name)
        val btnSave = view.findViewById<Button>(R.id.btn_save_name)
        profileImage = view.findViewById(R.id.profileImage)

        auth = FirebaseAuth.getInstance()

        val databaseUrl = "https://aa3appcompanionvalorant-default-rtdb.europe-west1.firebasedatabase.app/"
        val db = FirebaseDatabase.getInstance(databaseUrl)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            userRef = db.getReference("users").child(uid)
        }

        /*
         * Configuración de Google Sign-In para poder hacer signOut correctamente.
         * Aquí solo se pide el email (requestEmail).
         *
         * @returns {Unit}
         */
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        // Botón de cerrar sesión
        view.findViewById<Button>(R.id.signOutButton).setOnClickListener { signOut() }

        // Datos que pueden venir desde MainActivity al abrir Profile desde el menú
        val nameArg = arguments?.getString(ARG_USER_NAME)
        val imageArg = arguments?.getString(ARG_USER_IMAGE)

        // Usuario actual de Firebase (custom register / login)
        val firebaseUser = auth.currentUser

        // Cuenta de Google si el login fue con Google
        val googleAccount = GoogleSignIn.getLastSignedInAccount(requireContext())

        /*
         * Decide el nombre final:
         * - Prioriza argumentos (si los pasas desde MainActivity),
         * - si no, intenta displayName/email del usuario de Firebase,
         * - si no, displayName de Google,
         * - si no, "Player".
         *
         * @returns {Unit}
         */
        val finalName = nameArg
            ?: firebaseUser?.displayName
            ?: firebaseUser?.email
            ?: googleAccount?.displayName
            ?: "Player"

        /*
        * Opciones para esocger de avatares para el perfil
        */
        val btnChoose = view.findViewById<Button>(R.id.btn_choose_avatar)
        val btnClose = view.findViewById<Button>(R.id.btn_close_avatar_picker)
        val pickerLayout = view.findViewById<View>(R.id.layout_avatar_picker)
        val rv = view.findViewById<RecyclerView>(R.id.rv_avatars)

        val avatars = listOf(
            AvatarItem("default", R.drawable.avatar_sage),
            AvatarItem("omen", R.drawable.avatar_omen),
            AvatarItem("deadlock", R.drawable.avatar_deadlock),
            AvatarItem("viper", R.drawable.avatar_viper),
            AvatarItem("clove", R.drawable.avatar_clove)
        )

        rv.layoutManager = GridLayoutManager(requireContext(), 4)
        rv.adapter = AvatarAdapter(avatars) { selected ->
            // 1) Actualiza UI al instante
            profileImage.setImageResource(selected.drawableRes)

            // 2) Guarda en BD (users/<uid>/avatar = selected.id)
            if (uid != null) {
                userRef.child("avatar").setValue(selected.id)
            }

            // 3) Oculta selector
            pickerLayout.visibility = View.GONE
        }

        btnChoose.setOnClickListener { pickerLayout.visibility = View.VISIBLE }
        btnClose.setOnClickListener { pickerLayout.visibility = View.GONE }

        // --- Cargar avatar guardado (si existe) ---
        if (uid != null) {
            userRef.child("avatar").get().addOnSuccessListener { snap ->
                val avatarId = snap.getValue(String::class.java) ?: "default"
                val drawable = avatars.firstOrNull { it.id == avatarId }?.drawableRes ?: R.drawable.avatar_sage
                profileImage.setImageResource(drawable)
            }.addOnFailureListener {
                profileImage.setImageResource(R.drawable.avatar_sage)
            }
        } else {
            profileImage.setImageResource(R.drawable.avatar_sage)
        }
        // ----------------------------------------

        // --- Nombre: leer de DB y mostrar (fallback finalName) ---
        if (uid != null) {
            userRef.child("name").get().addOnSuccessListener { snap ->
                val dbName = snap.getValue(String::class.java)
                userName.text = dbName ?: finalName
                input.setText(userName.text.toString())
            }.addOnFailureListener {
                userName.text = finalName
                input.setText(userName.text.toString())
            }
        } else {
            userName.text = finalName
            input.setText(userName.text.toString())
        }
        // ------------------------------------------------------

        btnEdit.setOnClickListener {
            // Pre-cargar con el nombre actual
            input.setText(userName.text.toString())

            // Mostrar editor
            layoutEdit.visibility = View.VISIBLE
            input.requestFocus()
        }

        btnSave.setOnClickListener {
            val newName = input.text.toString().trim()
            if (newName.isEmpty()) return@setOnClickListener

            userName.text = newName
            layoutEdit.visibility = View.GONE

            if (uid != null) {
                userRef.child("name").setValue(newName)
            }
        }

        // NOTE:
        // Si estás usando opción 1 (avatares locales), no usamos imageArg/finalImage remoto,
        // porque podría pisar el avatar que el usuario eligió.
        // Si quieres que "imageArg" tenga prioridad sobre avatar, dímelo y lo ajustamos.
    }

    /*
     * Cierra sesión del usuario y vuelve al Login limpiando la pila de pantallas.
     * - auth.signOut() cierra Firebase
     * - googleSignInClient.signOut() cierra Google
     * - Flags NEW_TASK + CLEAR_TASK evitan volver atrás a MainActivity con el botón back.
     *
     * @returns {Unit} No devuelve nada.
     */
    private fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }

    companion object {
        private const val ARG_USER_NAME = "USER_NAME"
        private const val ARG_USER_IMAGE = "USER_IMAGE"

        /*
         * Constructor recomendado para crear el ProfileFragment con argumentos.
         * Evita usar setters y mantiene el patrón de Bundle para fragments.
         *
         * @param {String?} userName - Nombre del usuario a mostrar (si lo pasas desde MainActivity).
         * @param {String?} userImage - Imagen del usuario a mostrar (si la pasas desde MainActivity).
         * @returns {ProfileFragment} Fragment listo para usarse con arguments.
         */
        fun newInstance(userName: String?, userImage: String?) = ProfileFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_USER_NAME, userName)
                putString(ARG_USER_IMAGE, userImage)
            }
        }
    }
}
