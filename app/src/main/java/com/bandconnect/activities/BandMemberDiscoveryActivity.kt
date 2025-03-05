package com.bandconnect.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bandconnect.R
import com.bandconnect.adapters.BandMemberAdapter
import com.bandconnect.models.BandMember
import com.google.firebase.database.*

class BandMemberDiscoveryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var bandMemberAdapter: BandMemberAdapter
    private lateinit var database: DatabaseReference
    private val bandMembers = mutableListOf<BandMember>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_band_member_discovery)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("band_members")

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewBandMembers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        bandMemberAdapter = BandMemberAdapter(bandMembers) { bandMember ->
            // Handle band member click - Navigate to profile or start chat
        }
        recyclerView.adapter = bandMemberAdapter

        // Load band members from Firebase
        loadBandMembers()
    }

    private fun loadBandMembers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bandMembers.clear()
                for (memberSnapshot in snapshot.children) {
                    val bandMember = memberSnapshot.getValue(BandMember::class.java)
                    bandMember?.let { bandMembers.add(it) }
                }
                bandMemberAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}