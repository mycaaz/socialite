package com.bandconnect.activities

import com.bandconnect.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class MessageActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: MaterialButton
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var chatRef: DatabaseReference
    private var receiverId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        // Get receiver ID from intent
        receiverId = intent.getStringExtra("receiverId")
        if (receiverId == null) {
            finish()
            return
        }

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        chatRef = database.reference.child("chats")

        // Initialize views
        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupSendButton()
        loadMessages()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Messages"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Set up adapter for messages
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val messageText = messageInput.text?.toString()?.trim()
            if (messageText.isNullOrEmpty()) return@setOnClickListener

            val senderId = auth.currentUser?.uid ?: return@setOnClickListener
            val messageId = chatRef.push().key ?: return@setOnClickListener

            val message = hashMapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "message" to messageText,
                "timestamp" to System.currentTimeMillis()
            )

            chatRef.child(messageId).setValue(message)
                .addOnSuccessListener {
                    messageInput.text?.clear()
                }
        }
    }

    private fun loadMessages() {
        val currentUserId = auth.currentUser?.uid ?: return

        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // TODO: Update RecyclerView with messages
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}