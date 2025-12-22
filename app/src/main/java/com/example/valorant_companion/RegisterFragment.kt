package com.example.valorant_companion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth

/*
 * Fragment encargado del registro "custom" (email + password + username + foto).
 * - Crea el usuario con FirebaseAuth (email/password).
 * - Guarda "username" y "foto" SOLO a nivel de app (se pasan por Intent a MainActivity),
 *   no se guardan como displayName en Firebase.
 * - Registra en Firebase Analytics el tiempo que el usuario tarda en completar el registro.
 *
 * NOTA (IA):
 * - El selector de imagen con ActivityResultContracts.GetContent suele no estar explicado
 *   en apuntes básicos (depende del profe).
 * - Medir tiempo con SystemClock.elapsedRealtime() + evento "register_time" es una mejora.
 */
class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var usernameField: EditText
    private lateinit var profileImage: ImageView
    private var selectedImageUri: Uri? = null

    private lateinit var analytics: FirebaseAnalytics
    private var registerStartMs: Long = 0L

    /*
     * Launcher para abrir el selector de contenido (galería) y obtener una imagen.
     * Cuando el usuario elige una imagen:
     * - Guardas su Uri en selectedImageUri
     * - La muestras en profileImage
     */
    private val selectImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                profileImage.setImageURI(it)
            }
        }

    /*
     * Infla el layout del registro y prepara toda la lógica de UI:
     * - Inicializa FirebaseAuth y FirebaseAnalytics
     * - Conecta los EditText / ImageView
     * - Arranca el cronómetro para medir cuánto tarda el usuario en registrarse
     * - Listener para seleccionar imagen (click en profileImage)
     * - Botón registrar -> registerUser()
     * - Botón cancelar -> vuelve atrás (popBackStack)
     *
     * @param {LayoutInflater} inflater - Inflador del layout XML.
     * @param {ViewGroup?} container - Contenedor del fragment.
     * @param {Bundle?} savedInstanceState - Estado guardado.
     * @returns {View} Vista inflada del fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth = FirebaseAuth.getInstance()
        emailField = view.findViewById(R.id.input_register_email)
        passwordField = view.findViewById(R.id.input_register_password)
        usernameField = view.findViewById(R.id.input_register_username)
        profileImage = view.findViewById(R.id.image_register_profile)

        profileImage.setOnClickListener { selectImageLauncher.launch("image/*") }

        analytics = FirebaseAnalytics.getInstance(requireContext())
        registerStartMs = SystemClock.elapsedRealtime()

        view.findViewById<Button>(R.id.btn_fragment_register).setOnClickListener { registerUser() }

        view.findViewById<Button>(R.id.btn_fragment_cancel).setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        return view
    }

    /*
     * Hace el registro de usuario con Firebase Auth (email + password).
     * Flujo:
     * 1) Lee campos (email, password, username).
     * 2) Valida que no estén vacíos.
     * 3) createUserWithEmailAndPassword(...)
     *    - Si OK: abre MainActivity pasando extras USER_NAME y USER_IMAGE.
     *    - Registra en Analytics un evento "register_time" con:
     *        - duration_ms: tiempo total desde que se abrió el fragment
     *        - picked_image: si el usuario eligió foto o no
     *    - Cierra la activity actual (finish) para que no se pueda volver atrás.
     *    - Si error: log + toast con el mensaje.
     *
     */
    private fun registerUser() {
        val email = emailField.text.toString()
        val password = passwordField.text.toString()
        val username = usernameField.text.toString()

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(context, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                        putExtra("USER_NAME", username)
                        putExtra("USER_IMAGE", selectedImageUri?.toString() ?: "")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })

                    val durationMs = SystemClock.elapsedRealtime() - registerStartMs

                    val params = Bundle().apply {
                        putLong("duration_ms", durationMs)
                        putBoolean("picked_image", selectedImageUri != null)
                    }

                    analytics.logEvent("register_time", params)
                    requireActivity().finish()

                } else {
                    Log.e("RegisterFragment", "Error en el registro: ${task.exception?.message}")
                    Toast.makeText(
                        context,
                        "Error en el registro: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
