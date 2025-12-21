package com.example.valorant_companion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
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

    // 👇 para mostrar/ocultar
    private lateinit var mainLoginLayout: View
    private lateinit var fragmentContainer: FragmentContainerView

    companion object {
        private const val RC_GOOGLE = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Si ya hay sesión Firebase -> directo a Main
        auth.currentUser?.let {
            goMain()
            return
        }

        // refs UI
        mainLoginLayout = findViewById(R.id.main_login_layout)
        fragmentContainer = findViewById(R.id.fragment_container_view)

        // Listener para enseñar/ocultar según backstack (como en clase)
        supportFragmentManager.addOnBackStackChangedListener { updateLayoutVisibility() }
        updateLayoutVisibility()

        emailField = findViewById(R.id.input_email)
        passwordField = findViewById(R.id.input_password)

        findViewById<Button>(R.id.btn_login).setOnClickListener { loginEmail() }
        findViewById<Button>(R.id.btn_register).setOnClickListener { loadRegisterFragment() }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("310814606778-9l743rkti6f3eq78h75aai6uaj94fvu7.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<SignInButton>(R.id.btn_login_google).setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE)
        }
        
    }

    private fun updateLayoutVisibility() {
        val showingFragment = supportFragmentManager.backStackEntryCount > 0
        fragmentContainer.visibility = if (showingFragment) View.VISIBLE else View.GONE
        mainLoginLayout.visibility = if (showingFragment) View.GONE else View.VISIBLE
    }

    private fun loadRegisterFragment() {
        // (opcional pero ayuda) lo mostramos ya, y el listener remata
        fragmentContainer.visibility = View.VISIBLE
        mainLoginLayout.visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }

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
                else Toast.makeText(this, "Login error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) goMain()
                else Toast.makeText(this, "Google/Firebase error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

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
}
