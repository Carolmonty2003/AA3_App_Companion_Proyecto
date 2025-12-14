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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userName = view.findViewById(R.id.userName)
        profileImage = view.findViewById(R.id.profileImage)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        view.findViewById<Button>(R.id.signOutButton).setOnClickListener { signOut() }

        val nameArg = arguments?.getString(ARG_USER_NAME)
        val imageArg = arguments?.getString(ARG_USER_IMAGE)

        val firebaseUser = auth.currentUser
        val googleAccount = GoogleSignIn.getLastSignedInAccount(requireContext())

        val finalName = nameArg
            ?: firebaseUser?.displayName
            ?: firebaseUser?.email
            ?: googleAccount?.displayName
            ?: "Player"

        val finalImage = imageArg
            ?: firebaseUser?.photoUrl?.toString()
            ?: googleAccount?.photoUrl?.toString()
            ?: ""

        userName.text = finalName

        val defaultImageResId = R.drawable.ic_launcher_foreground

        if (finalImage.isBlank()) {
            profileImage.setImageResource(defaultImageResId)
        } else if (finalImage.startsWith("content://") || finalImage.startsWith("file://")) {
            profileImage.setImageURI(Uri.parse(finalImage))
        } else {
            loadImage(finalImage, defaultImageResId)
        }
    }

    private fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }

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

        fun newInstance(userName: String?, userImage: String?) = ProfileFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_USER_NAME, userName)
                putString(ARG_USER_IMAGE, userImage)
            }
        }
    }
}
