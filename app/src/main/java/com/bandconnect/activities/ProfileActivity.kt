package com.bandconnect.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.bandconnect.R

class ProfileActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var nameInput: TextInputEditText
    private lateinit var bioInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        initializeViews()
        setupToolbar()
        loadUserProfile()
        setupSaveButton()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        nameInput = findViewById(R.id.nameInput)
        bioInput = findViewById(R.id.bioInput)
        saveButton = findViewById(R.id.saveButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.reference.child("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                nameInput.setText(snapshot.child("name").value.toString())
                bioInput.setText(snapshot.child("bio").value.toString())
            }
        }
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val userRef = database.reference.child("users").child(userId)

            val updates = hashMapOf<String, Any>(
                "name" to (nameInput.text?.toString() ?: ""),
                "bio" to (bioInput.text?.toString() ?: "")
            )

            userRef.updateChildren(updates)
                .addOnSuccessListener {
                    // Show success message
                    finish()
                }
                .addOnFailureListener {
                    // Show error message
                }
        }
    }
}