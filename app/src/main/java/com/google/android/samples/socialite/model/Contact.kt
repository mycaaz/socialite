/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.samples.socialite.model

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User role enumeration for the music band community app
 * ADMIN - Special role with administrative privileges
 * BAND_MEMBER - Musicians in bands
 * FAN - Regular users who follow bands
 */
enum class UserRole {
    ADMIN, BAND_MEMBER, FAN
}

private val replyModels = mapOf<String, Contact.(String) -> Message.Builder>(
    "cat" to { _ -> buildReply { this.text = "Hi there! Need help with band promotion or community announcements? Let me know!" } },
    "dog" to { _ ->
        buildReply {
            this.text = "Check out our latest music video from our concert at Madison Square Garden!"
            mediaUri = "content://com.google.android.samples.socialite/video/mad_io23_recap.mp4"
            mediaMimeType = "video/mp4"
        }
    },
    "parrot" to { text -> buildReply { this.text = text } },
    "sheep" to { _ ->
        buildReply {
            this.text = "Just got tickets to the Feathered Notes show! So excited!"
            mediaUri = "content://com.google.android.samples.socialite/photo/sheep_full.jpg"
            mediaMimeType = "image/jpeg"
        }
    },
)

private const val SHORTCUT_PREFIX = "contact_"

@Entity
data class Contact(
    @PrimaryKey
    val id: Long,
    val name: String,
    val icon: String,
    val replyModel: String,
    val role: UserRole = UserRole.FAN,
    val bandName: String = "",
    val instrument: String = "",
    val genre: String = "",
    val bio: String = "",
    val location: String = "",
    val upcomingConcert: String = "",
) {
    companion object {
        val CONTACTS = listOf(
            // Admin
            Contact(
                id = 1L, 
                name = "BandManager", 
                icon = "cat.jpg", 
                replyModel = "cat", 
                role = UserRole.ADMIN,
                bandName = "Admins",
                bio = "I manage the community and help with band promotion."
            ),
            // Band members
            Contact(
                id = 2L, 
                name = "RockDog", 
                icon = "dog.jpg", 
                replyModel = "dog", 
                role = UserRole.BAND_MEMBER,
                bandName = "Barking Mad",
                instrument = "Guitar",
                genre = "Rock",
                bio = "Lead guitarist with a passion for classic rock",
                location = "Nashville, TN",
                upcomingConcert = "Sep 15, 2025 - The Ryman Auditorium"
            ),
            // More band members
            Contact(
                id = 3L, 
                name = "JazzParrot", 
                icon = "parrot.jpg", 
                replyModel = "parrot", 
                role = UserRole.BAND_MEMBER,
                bandName = "Feathered Notes",
                instrument = "Saxophone",
                genre = "Jazz",
                bio = "Award-winning saxophonist with 10 years experience",
                location = "New Orleans, LA"
            ),
            // Fan
            Contact(
                id = 4L, 
                name = "CountrySheep", 
                icon = "sheep.jpg", 
                replyModel = "sheep", 
                role = UserRole.FAN,
                bio = "Huge fan of country and bluegrass music",
                location = "Austin, TX"
            ),
        )
    }

    val iconUri: Uri
        get() = "content://com.google.android.samples.socialite/icon/$id".toUri()

    val contentUri: Uri
        get() = "https://socialite.google.com/chat/$id".toUri()

    val shortcutId: String
        get() = "$SHORTCUT_PREFIX$id"
        
    /**
     * Check if the contact has administrative privileges
     * @return true if the contact is an admin
     */
    fun isAdmin(): Boolean = role == UserRole.ADMIN
    
    /**
     * Check if the contact is a band member
     * @return true if the contact is a band member
     */
    fun isBandMember(): Boolean = role == UserRole.BAND_MEMBER
    
    /**
     * Check if the contact has an upcoming concert
     * @return true if the contact has an upcoming concert scheduled
     */
    fun hasUpcomingConcert(): Boolean = upcomingConcert.isNotEmpty()

    fun buildReply(body: Message.Builder.() -> Unit) = Message.Builder().apply {
        senderId = this@Contact.id
        timestamp = System.currentTimeMillis()
        body()
    }

    fun reply(text: String): Message.Builder {
        val model = replyModels[replyModel] ?: { _ -> buildReply { this.text = "Hello" } }
        return model(this, text)
    }
}

fun extractChatId(shortcutId: String): Long {
    if (!shortcutId.startsWith(SHORTCUT_PREFIX)) return 0L
    return try {
        shortcutId.substring(SHORTCUT_PREFIX.length).toLong()
    } catch (e: NumberFormatException) {
        0L
    }
}