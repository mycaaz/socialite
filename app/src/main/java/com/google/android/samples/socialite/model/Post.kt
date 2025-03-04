package com.google.android.samples.socialite.model

import java.util.UUID

enum class PostType {
    GENERAL_UPDATE,
    EVENT_ANNOUNCEMENT,
    NEW_RELEASE,
    BEHIND_THE_SCENES,
    MERCHANDISE,
    TOUR_UPDATE,
    FAN_EXCLUSIVE
}

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val content: String,
    val imageUrl: String? = null,
    val type: PostType = PostType.GENERAL_UPDATE,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val eventDetails: EventDetails? = null,
    val albumDetails: AlbumDetails? = null,
    val merchDetails: MerchDetails? = null,
    val isAdminOnly: Boolean = false,
    val isPinned: Boolean = false
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList()
)

data class EventDetails(
    val eventName: String,
    val location: String,
    val locationCoordinates: Location? = null,
    val date: Long,
    val endDate: Long? = null,
    val description: String,
    val ticketLink: String? = null,
    val isSoldOut: Boolean = false,
    val capacity: Int? = null,
    val venueDetails: String? = null
)

data class AlbumDetails(
    val albumName: String,
    val releaseDate: Long,
    val trackCount: Int,
    val streamingLinks: Map<String, String> = emptyMap(),
    val coverArtUrl: String? = null
)

data class MerchDetails(
    val itemName: String,
    val price: Double,
    val description: String,
    val shopLink: String,
    val isLimitedEdition: Boolean = false
)
