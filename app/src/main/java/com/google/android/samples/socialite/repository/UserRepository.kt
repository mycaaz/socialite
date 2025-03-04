package com.google.android.samples.socialite.data.repository

import com.google.android.samples.socialite.model.BandMemberInfo
import com.google.android.samples.socialite.model.FanType
import com.google.android.samples.socialite.model.Location
import com.google.android.samples.socialite.model.User
import com.google.android.samples.socialite.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {

    // Mock data for band members
    private val bandMembers = listOf(
        User(
            id = "user1",
            username = "johnlennon",
            email = "john@example.com",
            name = "John Lennon",
            bio = "Rhythm guitarist and vocalist for the Beatles",
            role = UserRole.BAND_MEMBER,
            profilePictureUrl = "https://example.com/john.jpg",
            bandInfo = BandMemberInfo(
                bandName = "The Beatles",
                role = "Lead Vocalist/Guitarist",
                instruments = listOf("Guitar", "Piano", "Harmonica"),
                yearsOfExperience = 20,
                genres = listOf("Rock", "Pop"),
                isVerified = true
            )
        ),
        User(
            id = "user2",
            username = "paulmccartney",
            email = "paul@example.com",
            name = "Paul McCartney",
            bio = "Bassist and vocalist for the Beatles",
            role = UserRole.BAND_MEMBER,
            profilePictureUrl = "https://example.com/paul.jpg",
            bandInfo = BandMemberInfo(
                bandName = "The Beatles",
                role = "Bassist/Vocalist",
                instruments = listOf("Bass", "Piano", "Guitar"),
                yearsOfExperience = 22,
                genres = listOf("Rock", "Pop"),
                isVerified = true
            )
        ),
        User(
            id = "user3",
            username = "davegrohl",
            email = "dave@example.com",
            name = "Dave Grohl",
            bio = "Founder of Foo Fighters, former drummer of Nirvana",
            role = UserRole.BAND_MEMBER,
            profilePictureUrl = "https://example.com/dave.jpg",
            bandInfo = BandMemberInfo(
                bandName = "Foo Fighters",
                role = "Lead Vocalist/Guitarist",
                instruments = listOf("Drums", "Guitar", "Vocals"),
                yearsOfExperience = 30,
                genres = listOf("Rock", "Alternative"),
                isVerified = true
            )
        ),
        User(
            id = "user4",
            username = "taylorswift",
            email = "taylor@example.com",
            name = "Taylor Swift",
            bio = "Award-winning singer-songwriter",
            role = UserRole.BAND_MEMBER,
            profilePictureUrl = "https://example.com/taylor.jpg",
            bandInfo = BandMemberInfo(
                bandName = "Taylor Swift",
                role = "Lead Vocalist/Songwriter",
                instruments = listOf("Guitar", "Piano", "Vocals"),
                yearsOfExperience = 18,
                genres = listOf("Pop", "Country", "Folk"),
                isVerified = true
            )
        ),
        User(
            id = "user5",
            username = "billie",
            email = "billie@example.com",
            name = "Billie Eilish",
            bio = "Singer-songwriter known for her unique style",
            role = UserRole.BAND_MEMBER,
            profilePictureUrl = "https://example.com/billie.jpg",
            bandInfo = BandMemberInfo(
                bandName = "Billie Eilish",
                role = "Vocalist/Songwriter",
                instruments = listOf("Vocals", "Piano"),
                yearsOfExperience = 8,
                genres = listOf("Pop", "Alternative", "Electropop"),
                isVerified = true
            )
        )
    )

    private val _users = MutableStateFlow<List<User>>(bandMembers)
    private val _currentUser = MutableStateFlow<User?>(null)

    val users: StateFlow<List<User>> = _users.asStateFlow()
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun getBandMembers(): Flow<List<User>> {
        return _users.map { users ->
            users.filter { it.role == UserRole.BAND_MEMBER }
        }
    }

    fun getBandMemberById(userId: String): Flow<User?> {
        return _users.map { users ->
            users.firstOrNull { it.id == userId }
        }
    }

    fun getFans(): Flow<List<User>> {
        return _users.map { users ->
            users.filter { it.role == UserRole.FAN }
        }
    }

    suspend fun createUser(
        username: String,
        email: String,
        name: String,
        isBandMember: Boolean = false,
        bandInfo: BandMemberInfo? = null,
        fanType: FanType? = FanType.CASUAL_LISTENER
    ): User {
        val role = if (isBandMember) UserRole.BAND_MEMBER else UserRole.FAN
        val user = User(
            username = username,
            email = email,
            name = name,
            role = role,
            bandInfo = bandInfo,
            fanType = if (isBandMember) null else fanType
        )
        val currentList = _users.value.toMutableList()
        currentList.add(user)
        _users.value = currentList
        return user
    }

    suspend fun login(email: String, password: String): User? {
        // In a real app, this would validate credentials against a backend
        val user = _users.value.firstOrNull { it.email == email }
        _currentUser.value = user
        return user
    }

    suspend fun logout() {
        _currentUser.value = null
    }

    suspend fun updateUserLocation(userId: String, location: Location) {
        val updatedUsers = _users.value.map { user ->
            if (user.id == userId) {
                user.copy(location = location)
            } else {
                user
            }
        }
        _users.value = updatedUsers
    }

    suspend fun updateUserProfile(userId: String, updatedUser: User) {
        val updatedUsers = _users.value.map { user ->
            if (user.id == userId) updatedUser else user
        }
        _users.value = updatedUsers

        if (_currentUser.value?.id == userId) {
            _currentUser.value = updatedUser
        }
    }

    suspend fun followBandMember(userId: String, bandMemberId: String) {
        val updatedUsers = _users.value.map { user ->
            when (user.id) {
                userId -> {
                    val following = user.following.toMutableList()
                    if (!following.contains(bandMemberId)) {
                        following.add(bandMemberId)
                    }
                    user.copy(following = following)
                }
                bandMemberId -> {
                    val followers = user.followers.toMutableList()
                    if (!followers.contains(userId)) {
                        followers.add(userId)
                    }
                    user.copy(followers = followers)
                }
                else -> user
            }
        }
        _users.value = updatedUsers
    }

    suspend fun unfollowBandMember(userId: String, bandMemberId: String) {
        val updatedUsers = _users.value.map { user ->
            when (user.id) {
                userId -> {
                    val following = user.following.toMutableList()
                    following.remove(bandMemberId)
                    user.copy(following = following)
                }
                bandMemberId -> {
                    val followers = user.followers.toMutableList()
                    followers.remove(userId)
                    user.copy(followers = followers)
                }
                else -> user
            }
        }
        _users.value = updatedUsers
    }

    suspend fun login(email: String, password: String): User? {
        // For demo purposes, always log in as the first band member
        val user = _users.value.firstOrNull { it.role == UserRole.BAND_MEMBER }
        _currentUser.value = user
        return user
    }
}
