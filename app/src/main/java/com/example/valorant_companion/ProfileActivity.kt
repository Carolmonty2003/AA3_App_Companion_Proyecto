package com.example.valorant_companion

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ProfileActivity : AppCompatActivity() {

    private lateinit var userName: TextView
    private lateinit var profileImage: ImageView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userName = findViewById(R.id.userName)
        profileImage = findViewById(R.id.profileImage)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<Button>(R.id.signOutButton).setOnClickListener{ SignOut() }

        userName.text = intent.getStringExtra( "USER_NAME")

        val defaultImageResId = R.drawable.ic_launcher_foreground

        intent.getStringExtra("USER_IMAGE")?.let { uriOrUrlString ->
            if (uriOrUrlString.isEmpty()) {
                // Caso String vacío (Usuario no seleccionó imagen en RegisterFragment)
                profileImage.setImageResource(defaultImageResId)
            }
            // Caso URI local (Usuario seleccionó imagen en RegisterFragment)
            else if (uriOrUrlString.startsWith("content://") || uriOrUrlString.startsWith("file://")) {
                profileImage.setImageURI(Uri.parse(uriOrUrlString))
            }
            // Caso URL remota (ej. Google Sign-In, u otra URL)
            else {
                loadImage(uriOrUrlString)
            }
        } ?: run {
            // Caso 'USER_IMAGE' es nulo (seguro de error)
            profileImage.setImageResource(defaultImageResId)
        }
    }

    private fun SignOut(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        auth.signOut()
    }

    private fun loadImage(urlString: String){
        Thread{
            try{
                val url = URL(urlString)

                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val input: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)

                runOnUiThread{
                    profileImage.setImageBitmap(bitmap)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }.start()
    }
}