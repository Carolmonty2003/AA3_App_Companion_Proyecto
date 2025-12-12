package com.example.valorant_companion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var usernameField: EditText
    private lateinit var profileImage: ImageView
    private var selectedImageUri: Uri? = null

    // Launcher para seleccionar una imagen de la galería
    private val selectImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                profileImage.setImageURI(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth = FirebaseAuth.getInstance()
        emailField = view.findViewById(R.id.input_register_email)
        passwordField = view.findViewById(R.id.input_register_password)
        usernameField = view.findViewById(R.id.input_register_username)
        profileImage = view.findViewById(R.id.image_register_profile)

        // Botón para seleccionar imagen
        profileImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        // Botón para completar el registro
        view.findViewById<Button>(R.id.btn_fragment_register).setOnClickListener {
            registerUser()
        }

        // Botón para volver a la pantalla de inicio/login
        view.findViewById<Button>(R.id.btn_fragment_cancel).setOnClickListener {
            // Usa popBackStack para cerrar el fragment.
            // Esto dispara el OnBackStackChangedListener en MainActivity.
            activity?.supportFragmentManager?.popBackStack()
        }

        return view
    }

    private fun registerUser() {
        val email = emailField.text.toString()
        val password = passwordField.text.toString()
        val username = usernameField.text.toString()

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(context, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Registro en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()

                    // TODO: Aquí se debería implementar la subida de la imagen y guardar el nombre de usuario
                    // Por simplicidad, pasaremos el URI de la imagen seleccionada y el nombre de usuario
                    // al ProfileActivity directamente.

                    val intent = Intent(activity, ProfileActivity::class.java).apply {
                        putExtra("USER_NAME", username)
                        // Usa el URI de la imagen seleccionada, o un placeholder si no hay
                        putExtra("USER_IMAGE", selectedImageUri?.toString() ?: "")
                    }
                    startActivity(intent)

                } else {
                    Log.e("RegisterFragment", "Error en el registro: ${task.exception?.message}")
                    Toast.makeText(context, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}