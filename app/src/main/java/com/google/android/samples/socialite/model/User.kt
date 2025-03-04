package com.google.android.samples.socialite.model

import java.util.UUID

enum class UserRole {
    FAN,
    BAND_MEMBER,
    ADMIN
}

enum class FanType {
    CASUAL_LISTENER,
    DEDICATED_FAN,
    SUPERFAN
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    val name: String,
    val profilePictureUrl: String = "",
    val bio: String = "",
    val role: UserRole = UserRole.FAN,
    val fanType: FanType? = null,
    val bandInfo: BandMemberInfo? = null,
    val location: Location? = null,
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val lastActive: Long = System.currentTimeMillis(),
    val notificationsEnabled: Boolean = true,
    val locationSharingEnabled: Boolean = false,
    val preferredGenres: List<String> = emptyList(),
    val socialLinks: Map<String, String> = emptyMap()
)

data class BandMemberInfo(
    val bandName: String,
    val role: String,
    val instruments: List<String> = emptyList(),
    val yearsOfExperience: Int = 0,
    val genres: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val albumsReleased: Int = 0,
    val upcomingEvents: List<String> = emptyList(),
    val quickResponseEnabled: Boolean = true
)
