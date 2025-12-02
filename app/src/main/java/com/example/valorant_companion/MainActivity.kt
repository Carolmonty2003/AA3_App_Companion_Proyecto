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

class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //button = findViewById(R.id.button)
        /*button.setOnClickListener {
            val intent = Intent(this, SplashScreenActivity::class.java)
            startActivity(intent)
        }*/

        //inicio sesion normal
        emailField = findViewById(R.id.input_email)
        passwordField = findViewById(R.id.input_password)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.btn_register).setOnClickListener{Register()}
        findViewById<Button>(R.id.btn_login).setOnClickListener{Login()}

        //inicio sesion google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("310814606778-9l743rkti6f3eq78h75aai6uaj94fvu7.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val account = GoogleSignIn.getLastSignedInAccount(this)

        account?.let{
            //Log.d("Login Google", "Ya se ha robado la info de: " + account.displayName + "anteriorimente")
            //startActivity(Intent(this, ProfileActivity::class.java))
            LoginSuccess(account)
        } ?: run {
            Log.d("Login Google", "No hay sesion iniciada")
            findViewById<SignInButton>(R.id.btn_login_google).setOnClickListener{SignIn()}
        }

        googleSignInClient.signOut().addOnCompleteListener(this){
            //cod a ejecutar tras el signout normlamnete un redureccionamiento a login
        }

        //inicio sesion con otros...
    }

    private fun Register(){
        val email = emailField.text.toString()
        val password = passwordField.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun Login(){
        val email = emailField.text.toString()
        val password = passwordField.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    val user = auth.currentUser //obtiene usuario actual
                    val userId = user?.uid //obtiene el ID del usuario


                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ProfileActivity::class.java).apply{
                        //putExtra("USER_NAME", userName)
                        //putExtra("USER_IMAGE", userImage.toString())
                    }
                }
                else{
                    Toast.makeText(this, "Error en el inicio de sesión: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun SignIn(){
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
    }

    private fun LoginSuccess(account: GoogleSignInAccount){
        val userName = account.displayName
        val userImage = account.photoUrl

        val intent = Intent(this, ProfileActivity::class.java).apply{
            putExtra("USER_NAME", userName)
            putExtra("USER_IMAGE", userImage.toString())
        }
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 9001){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data) //procesa la info
            if(task.isSuccessful){
                val account = task.getResult(ApiException::class.java)
                //Log.d("Login Google", "Tengo la info de: " + account.displayName)
                LoginSuccess(account)
            }else {
                Log.d("Login Google", "Error la info de: " + task.exception)
            }
        }
    }
}