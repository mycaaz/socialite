package com.bandconnect.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bandconnect.R
import com.bandconnect.models.BandMember
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class BandMemberProfileActivity : AppCompatActivity() {
    private lateinit var imageViewProfile: ImageView
    private lateinit var textViewName: TextView
    private lateinit var textViewInstrument: TextView
    private lateinit var textViewBio: TextView
    private lateinit var textViewLocation: TextView
    private var googleMap: GoogleMap? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_band_member_profile)

        // Initialize views
        imageViewProfile = findViewById(R.id.imageViewProfile)
        textViewName = findViewById(R.id.textViewName)
        textViewInstrument = findViewById(R.id.textViewInstrument)
        textViewBio = findViewById(R.id.textViewBio)
        textViewLocation = findViewById(R.id.textViewLocation)

        // Get band member data from intent
        val bandMember = intent.getParcelableExtra("band_member", BandMember::class.java)
        bandMember?.let { member ->
            displayBandMemberInfo(member)
            setupMap(member)
        }
    }

    private fun displayBandMemberInfo(bandMember: BandMember) {
        textViewName.text = bandMember.name
        textViewInstrument.text = bandMember.instrument
        textViewBio.text = bandMember.bio
        textViewLocation.text = bandMember.location.address

        Glide.with(this)
            .load(bandMember.imageUrl)
            .placeholder(R.drawable.default_profile)
            .circleCrop()
            .into(imageViewProfile)
    }

    private fun setupMap(bandMember: BandMember) {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            googleMap = map
            val location = LatLng(bandMember.location.latitude, bandMember.location.longitude)
            map.addMarker(MarkerOptions().position(location).title(bandMember.name))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }
}