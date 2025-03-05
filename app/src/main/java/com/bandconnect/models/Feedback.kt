package com.bandconnect.models

data class Feedback(
    val id: String = "",
    val userId: String = "",
    val bandId: String = "",
    val rating: Float = 0.0f,
    val review: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", 0.0f, "", 0L)
}