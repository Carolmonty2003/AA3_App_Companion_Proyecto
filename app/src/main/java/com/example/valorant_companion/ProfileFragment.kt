package com.example.valorant_companion

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var userName: TextView
    private lateinit var profileImage: ImageView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

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

        userName = view.findViewById(R.id.userName)
        profileImage = view.findViewById(R.id.profileImage)

        auth = FirebaseAuth.getInstance()

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
         * Decide la imagen final:
         * - Prioriza argumentos,
         * - si no, photoUrl de Firebase,
         * - si no, photoUrl de Google,
         * - si no, vacío.
         *
         * @returns {Unit}
         */
        val finalImage = imageArg
            ?: firebaseUser?.photoUrl?.toString()
            ?: googleAccount?.photoUrl?.toString()
            ?: ""

        userName.text = finalName

        val defaultImageResId = R.drawable.ic_launcher_foreground

        /*
         * Carga la imagen según el origen:
         * - Si no hay imagen -> icono por defecto.
         * - Si es URI local (content:// o file://) -> setImageURI.
         * - Si es URL remota (http/https) -> se descarga en un Thread.
         *
         * @returns {Unit}
         */
        if (finalImage.isBlank()) {
            profileImage.setImageResource(defaultImageResId)
        } else if (finalImage.startsWith("content://") || finalImage.startsWith("file://")) {
            profileImage.setImageURI(Uri.parse(finalImage))
        } else {
            loadImage(finalImage, defaultImageResId)
        }
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

    /*
     * Descarga una imagen desde una URL y la pinta en el ImageView.
     * Lo hace en un Thread para no bloquear la UI, y luego actualiza la UI con runOnUiThread.
     *
     * IMPORTANTE:
     * - isAdded evita crasheos si el fragment ya no está “attached” cuando termina la descarga.
     * - fallbackResId se usa si falla la conexión/descarga.
     *
     * @param {String} urlString - URL remota (http/https) de la imagen.
     * @param {Int} fallbackResId - Recurso drawable por defecto si falla.
     * @returns {Unit} No devuelve nada.
     */
    private fun loadImage(urlString: String, fallbackResId: Int) {
        Thread {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val input: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)

                if (isAdded) {
                    requireActivity().runOnUiThread {
                        profileImage.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        profileImage.setImageResource(fallbackResId)
                    }
                }
            }
        }.start()
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
