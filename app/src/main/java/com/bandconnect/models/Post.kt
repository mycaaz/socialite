package com.bandconnect.models

import com.google.firebase.database.ServerValue

data class Post(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "image" or "video"
    val timestamp: Any = ServerValue.TIMESTAMP,
    val likes: Map<String, Boolean> = HashMap(),
    val comments: List<Comment> = ArrayList()
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "")
}

data class Comment(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Any = ServerValue.TIMESTAMP
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "")
}