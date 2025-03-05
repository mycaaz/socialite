package com.bandconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bandconnect.activities.LoginActivity
import com.bandconnect.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()

            // Set up bottom navigation with navigation controller
            val navController = findNavController(R.id.nav_host_fragment)
            binding.bottomNavigationView.setupWithNavController(navController)

            // Check if user is signed in
            checkAuthState()
        } catch (e: Exception) {
            // Log the error and show a message
            Log.e("MainActivity", "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User is not signed in, navigate to login screen
            // TODO: Implement navigation to login screen
            if (currentUser == null) {
                // User is not signed in, navigate to login screen
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Close MainActivity so user can't go back
            }
        }
    }
}