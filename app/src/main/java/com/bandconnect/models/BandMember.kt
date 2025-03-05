package com.bandconnect.models

data class BandMember(
    val id: String = "",
    val name: String = "",
    val instrument: String = "",
    val bio: String = "",
    val imageUrl: String = "",
    val location: Location = Location(),
    val isAdmin: Boolean = false
) {
    data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val address: String = ""
    )
}