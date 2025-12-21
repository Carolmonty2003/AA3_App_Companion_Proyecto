package com.example.valorant_companion

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var auth: FirebaseAuth

    companion object {
        /*
         * Código de petición para distinguir el resultado del login de Google en onActivityResult.
         */
        private const val RC_GOOGLE = 9001
    }

    /*
     * Punto de entrada de la pantalla de Login.
     *
     * Qué hace:
     * - Inicializa FirebaseAuth
     * - Si ya hay sesión iniciada -> entra directo a Main
     * - Prepara login por Email/Password
     * - Prepara login con Google
     *     *
     * @param {Bundle?} savedInstanceState - Estado guardado de la Activity (si existe).
     * @returns {Unit}
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Si ya hay sesión Firebase -> directo a Main
        auth.currentUser?.let {
            goMain()
            return
        }

        emailField = findViewById(R.id.input_email)
        passwordField = findViewById(R.id.input_password)

        // Login clásico email/password
        findViewById<Button>(R.id.btn_login).setOnClickListener { loginEmail() }

        // Carga el RegisterFragment dentro de la propia LoginActivity
        findViewById<Button>(R.id.btn_register).setOnClickListener { loadRegisterFragment() }

        //Configuración del login de Google.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("310814606778-9l743rkti6f3eq78h75aai6uaj94fvu7.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        /*
         * Lanzamos el intent de Google Sign-In.
         */
        findViewById<SignInButton>(R.id.btn_login_google).setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE)
        }
    }

    /*
     * Login con Email y Password usando FirebaseAuth.
     *
     * Flujo:
     * - Leer inputs
     * - Validar vacíos
     * - auth.signInWithEmailAndPassword(...)
     * - Si OK -> goMain()
     * - Si falla -> Toast con error
     *
     * @returns {Unit}
     */
    private fun loginEmail() {
        val email = emailField.text.toString().trim()
        val pass = passwordField.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Rellena email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) goMain()
                else Toast.makeText(
                    this,
                    "Login error: ${task.exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /*
     * Intercambia el resultado de Google Sign-In por una sesión de Firebase.
     *
     * Este paso es clave:
     * - GoogleAuthProvider.getCredential(idToken, null)
     * - auth.signInWithCredential(credential)
     *
     * @param {GoogleSignInAccount} account - Cuenta devuelta por Google Sign-In.
     * @returns {Unit}
     */
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goMain()
                } else {
                    Toast.makeText(
                        this,
                        "Google/Firebase error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /*
     * Navega a MainActivity y cierra LoginActivity para que no vuelva atrás con el botón Back.
     *
     * @returns {Unit}
     */
    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /*
     * Recibe el resultado del intent de Google Sign-In.
     * Si requestCode coincide con RC_GOOGLE:
     * - intenta recuperar la cuenta
     * - si OK -> firebaseAuthWithGoogle(account)
     * - si falla -> Log + Toast
     *
     * (IA): El Log.e detallado es típico para depurar.
     *
     * @param {Int} requestCode - Código que identifica qué “resultado” vuelve.
     * @param {Int} resultCode - Resultado de la activity (OK/CANCELLED).
     * @param {Intent?} data - Datos de vuelta (incluye la cuenta de Google).
     * @returns {Unit}
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: Exception) {
                Log.e("LOGIN", "Google sign in failed", e)
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    /*
     * Carga el RegisterFragment dentro del contenedor de LoginActivity.
     * Se añade al backstack para que al darle “atrás” vuelva al login.
     *
     * @returns {Unit}
     */
    private fun loadRegisterFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }
}
