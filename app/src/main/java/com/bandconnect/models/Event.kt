package com.bandconnect.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Event(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0,
    val location: String = "",
    val bandId: String = "",
    val maxCapacity: Int = 0,
    val currentRegistrations: Int = 0,
    val imageUrl: String? = null,
    val status: String = "UPCOMING", // UPCOMING, ONGOING, COMPLETED, CANCELLED
    val registeredUsers: Map<String, Boolean> = mapOf()
) {
    // Empty constructor required for Firebase
    constructor() : this("", "", "", 0, "", "", 0)
}