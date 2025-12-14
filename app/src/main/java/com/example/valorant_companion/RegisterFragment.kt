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
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth = FirebaseAuth.getInstance()
        emailField = view.findViewById(R.id.input_register_email)
        passwordField = view.findViewById(R.id.input_register_password)
        usernameField = view.findViewById(R.id.input_register_username)
        profileImage = view.findViewById(R.id.image_register_profile)

        profileImage.setOnClickListener { selectImageLauncher.launch("image/*") }

        view.findViewById<Button>(R.id.btn_fragment_register).setOnClickListener { registerUser() }

        view.findViewById<Button>(R.id.btn_fragment_cancel).setOnClickListener {
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

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                        putExtra("USER_NAME", username)
                        putExtra("USER_IMAGE", selectedImageUri?.toString() ?: "")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
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
