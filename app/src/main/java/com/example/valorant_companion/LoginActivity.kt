package com.example.valorant_companion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicio sesión normal
        emailField = findViewById(R.id.input_email)
        passwordField = findViewById(R.id.input_password)
        auth = FirebaseAuth.getInstance()

        // Listener del back stack para mostrar/ocultar el fragment de register
        supportFragmentManager.addOnBackStackChangedListener { updateLayoutVisibility() }
        updateLayoutVisibility()

        findViewById<Button>(R.id.btn_login).setOnClickListener { Login() }
        findViewById<Button>(R.id.btn_register).setOnClickListener { loadRegisterFragment() }

        // Inicio sesión Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("310814606778-9l743rkti6f3eq78h75aai6uaj94fvu7.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        account?.let {
            // Si ya hay cuenta Google, entra directo
            LoginSuccess(it)
        } ?: run {
            Log.d("Login Google", "No hay sesión iniciada")
            findViewById<SignInButton>(R.id.btn_login_google).setOnClickListener { SignIn() }
        }

        // TEMP (si tienes el botón en el XML)
        val tempBtn = findViewById<Button?>(R.id.btn_go_main)
        tempBtn?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    // Visibilidad del layout principal vs. contenedor de fragment (register)
    private fun updateLayoutVisibility() {
        val fragmentContainer = findViewById<View>(R.id.fragment_container_view)
        val mainLoginLayout = findViewById<View>(R.id.main_login_layout)

        if (supportFragmentManager.backStackEntryCount > 0) {
            mainLoginLayout.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
        } else {
            mainLoginLayout.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE
        }
    }

    private fun loadRegisterFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) super.onBackPressed()
        else super.onBackPressed()
    }

    private fun Login() {
        val email = emailField.text.toString()
        val password = passwordField.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese email y contraseña.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val userName = user?.email ?: "Usuario desconocido"
                    val userImageUri = user?.photoUrl?.toString() ?: ""

                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtra("USER_NAME", userName)
                        putExtra("USER_IMAGE", userImageUri)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Error en el inicio de sesión: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun SignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
    }

    private fun LoginSuccess(account: GoogleSignInAccount) {
        val userName = account.displayName ?: "Player"
        val userImage = account.photoUrl?.toString() ?: ""

        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("USER_NAME", userName)
            putExtra("USER_IMAGE", userImage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.getResult(ApiException::class.java)
                LoginSuccess(account)
            } else {
                Log.d("Login Google", "Error: ${task.exception}")
            }
        }
    }
}
