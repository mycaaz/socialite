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

package com.google.android.samples.socialite.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.LocusIdCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.google.android.samples.socialite.BubbleActivity
import com.google.android.samples.socialite.MainActivity
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.ReplyReceiver
import com.google.android.samples.socialite.model.Contact
import com.google.android.samples.socialite.model.Message
import com.google.android.samples.socialite.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "SocialiteMusicBandPrefs"
private const val KEY_PRIORITY_NOTIFICATIONS = "priority_notifications_enabled"

/**
 * Notification channels for messages, depending on their priority and role
 */
enum class NotificationChannelType {
    DEFAULT,
    PRIORITY,  // For high-priority messages from band members
    CONCERT    // For concert announcement notifications
}

/**
 * Extension property to get the channel ID for each notification channel type
 */
private val NotificationChannelType.channelId: String
    get() = when (this) {
        NotificationChannelType.DEFAULT -> "messages"
        NotificationChannelType.PRIORITY -> "priority_messages"
        NotificationChannelType.CONCERT -> "concert_announcements"
    }

/**
 * Handles all notification related work in one place.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        /**
         * The notification channel for messages.
         */
        private const val CHANNEL_MESSAGES = "messages"
        
        /**
         * The notification channel for priority messages from band members.
         */
        private const val CHANNEL_PRIORITY_MESSAGES = "priority_messages"
        
        /**
         * The notification channel for concert announcements.
         */
        private const val CHANNEL_CONCERT_ANNOUNCEMENTS = "concert_announcements"

        /**
         * The request code for the reply action's PendingIntent.
         */
        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2

        /**
         * The request code for the "Reply" action's PendingIntent.
         */
        private const val REQUEST_REPLY = 0

        /**
         * The notification ID for the foreground conversation.
         */
        const val NOTIFICATION_ID = 1
    }
    
    // Shared Preferences for various settings
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Check if priority notifications are enabled
    private fun arePriorityNotificationsEnabled(): Boolean = 
        prefs.getBoolean(KEY_PRIORITY_NOTIFICATIONS, false)

    /**
     * Creates a messaging style notification for a message from the specified [contact].
     */
    fun showNotification(contact: Contact, message: Message, fromUser: Boolean) {
        try {
            val shortcutId = contact.shortcutId
            val chatId = message.chatId

            // Create/update the notification.
            val notificationManager = context.getSystemService<NotificationManager>()
            if (notificationManager != null) {
                // Determine notification channel based on role and priority
                val channelType = determineNotificationChannel(contact)
                
                // Create notification channels
                createNotificationChannels(notificationManager)

                val icon = loadContactIcon(contact)
                val person = Person.Builder()
                    .setName(contact.name)
                    .setIcon(icon)
                    .build()
                val messagingStyle = NotificationCompat.MessagingStyle(person)
                    .addMessage(message.text, message.timestamp, person)
                    .setGroupConversation(false)
                    .setConversationTitle(contact.name)

                val builder = NotificationCompat.Builder(context, channelType.channelId)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setShowWhen(true)
                    .setStyle(messagingStyle)
                    .setShortcutId(shortcutId)
                    .setLocusId(LocusIdCompat(shortcutId))
                    .setSmallIcon(R.drawable.ic_message)
                    .setBubbleMetadata(
                        NotificationCompat.BubbleMetadata.Builder(
                            createBubbleIntent(contact),
                            icon,
                        )
                            .setDesiredHeight(600)
                            .setSuppressNotification(fromUser)
                            .build(),
                    )
                    .addAction(createReplyAction(chatId))

                // Add priority flags for band member notifications if enabled
                if (channelType == NotificationChannelType.PRIORITY) {
                    builder.priority = NotificationCompat.PRIORITY_HIGH
                    builder.setVibrate(longArrayOf(0, 300, 300, 300))
                }
                
                // Create custom notification for concert announcements
                if (channelType == NotificationChannelType.CONCERT && contact.upcomingConcert.isNotEmpty()) {
                    builder
                        .setContentTitle("Concert Announcement: ${contact.name}")
                        .setContentText(message.text)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(message.text))
                        .addAction(createViewConcertAction(contact))
                }

                notificationManager.notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: Exception) {
            // Log the error or handle it gracefully
            // We don't want to crash if notification building fails
        }
    }
    
    /**
     * Determines which notification channel to use based on contact role and message content
     */
    private fun determineNotificationChannel(contact: Contact): NotificationChannelType {
        // Check if the contact is a band member and priority notifications are enabled
        if (contact.role == UserRole.BAND_MEMBER && arePriorityNotificationsEnabled()) {
            return NotificationChannelType.PRIORITY
        }
        
        // Check if this is a concert announcement
        if (contact.hasUpcomingConcert()) {
            return NotificationChannelType.CONCERT
        }
        
        // Default channel for regular messages
        return NotificationChannelType.DEFAULT
    }
    
    /**
     * Creates an action to view concert details
     */
    private fun createViewConcertAction(contact: Contact): NotificationCompat.Action {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("CONTACT_ID", contact.id)
            putExtra("VIEW_CONCERT", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "View Concert Details",
            pendingIntent
        ).build()
    }

    /**
     * Creates a PendingIntent that opens up the bubble.
     */
    private fun createBubbleIntent(contact: Contact): PendingIntent {
        val chatBubbleIntent = Intent(context, BubbleActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .setData(
                "https://socialite.google.com/chat/${contact.id}".toUri(),
            )
        return PendingIntent.getActivity(
            context,
            REQUEST_BUBBLE,
            chatBubbleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /**
     * Creates a reply action for the specified [chatId].
     */
    private fun createReplyAction(chatId: Long): NotificationCompat.Action {
        val replyIntent = Intent(context, ReplyReceiver::class.java)
            .setAction(ReplyReceiver.ACTION_REPLY)
            .putExtra(ReplyReceiver.EXTRA_CHAT_ID, chatId.toString())
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_REPLY,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

        val remoteInput = RemoteInput.Builder(ReplyReceiver.EXTRA_TEXT_REPLY)
            .setLabel(context.getString(R.string.label_reply))
            .build()

        return NotificationCompat.Action.Builder(
            R.drawable.ic_send,
            context.getString(R.string.label_reply),
            replyPendingIntent,
        )
            .addRemoteInput(remoteInput)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .setShowsUserInterface(false)
            .build()
    }

    /**
     * Creates notification channels for different types of notifications
     */
    private fun createNotificationChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Default channel
                val defaultChannel = NotificationChannel(
                    NotificationChannelType.DEFAULT.channelId,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Regular messages from users"
                }
                
                // Priority channel for band members
                val priorityChannel = NotificationChannel(
                    NotificationChannelType.PRIORITY.channelId,
                    "Priority Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High priority messages from band members"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 300, 300)
                }
                
                // Concert announcements channel
                val concertChannel = NotificationChannel(
                    NotificationChannelType.CONCERT.channelId,
                    "Concert Announcements",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Upcoming concert announcements"
                    enableVibration(true)
                }
                
                notificationManager.createNotificationChannels(
                    listOf(defaultChannel, priorityChannel, concertChannel)
                )
            } catch (e: Exception) {
                // Handle channel creation errors
            }
        }
    }

    /**
     * Loads an icon for the [contact].
     * Falls back to the app icon if there's any issue with contact icons.
     */
    private fun loadContactIcon(contact: Contact): IconCompat {
        try {
            // Try to load the contact's icon, but if it fails, fall back to the app icon
            val icon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
            return IconCompat.createWithAdaptiveBitmap(icon)
        } catch (e: Exception) {
            // If anything goes wrong, use the app icon
            val appIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
            return IconCompat.createWithAdaptiveBitmap(appIcon)
        }
    }
}