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

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.android.samples.socialite.BuildConfig
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.data.ChatDao
import com.google.android.samples.socialite.data.ContactDao
import com.google.android.samples.socialite.data.MessageDao
import com.google.android.samples.socialite.data.utils.ShortsVideoList
import com.google.android.samples.socialite.di.AppCoroutineScope
import com.google.android.samples.socialite.model.ChatDetail
import com.google.android.samples.socialite.model.Message
import com.google.android.samples.socialite.widget.model.WidgetModelRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Push reasons for messages
 */
enum class PushReason {
    IncomingMessage,
    OutgoingMessage
}

@Singleton
class ChatRepository @Inject internal constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val contactDao: ContactDao,
    private val notificationHelper: NotificationHelper,
    private val widgetModelRepository: WidgetModelRepository,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
    @ApplicationContext private val appContext: Context,
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val enableChatbotKey = booleanPreferencesKey("enable_chatbot")
    val isBotEnabled = appContext.dataStore.data.map { preference ->
        preference[enableChatbotKey] ?: false
    }

    private var currentChat: Long = 0L

    fun getChats(): Flow<List<ChatDetail>> {
        return try {
            chatDao.allDetails().catch { emit(emptyList()) }
        } catch (e: Exception) {
            emptyFlow()
        }
    }

    fun findChat(chatId: Long): Flow<ChatDetail?> {
        return try {
            chatDao.detailById(chatId).catch { emit(null) }
        } catch (e: Exception) {
            emptyFlow()
        }
    }

    fun findMessages(chatId: Long): Flow<List<Message>> {
        return try {
            messageDao.allByChatId(chatId).catch { emit(emptyList()) }
        } catch (e: Exception) {
            emptyFlow()
        }
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        mediaUri: String?,
        mediaMimeType: String?,
    ) {
        try {
            val detail = try {
                chatDao.loadDetailById(chatId)
            } catch (e: Exception) {
                // If we can't load the chat detail, we can't proceed
                null
            } ?: return
            
            // Save the message to the database
            saveMessageAndNotify(chatId, text, 0L, mediaUri, mediaMimeType, detail, PushReason.OutgoingMessage)

            coroutineScope.launch {
                try {
                    // Special incoming message indicating to add shorts videos to try preload in exoplayer
                    if (text == "preload") {
                        preloadShortVideos(chatId, detail, PushReason.IncomingMessage)
                        return@launch
                    }
    
                    // Simulate a response from the peer.
                    // The code here is just for demonstration purpose in this sample.
                    // Real apps will use their server backend and Firebase Cloud Messaging to deliver messages.
    
                    // The person is typing...
                    delay(2000L)
                    // Receive a reply.
                    val message = detail.firstContact.reply(text).apply { this.chatId = chatId }.build()
                    saveMessageAndNotify(message.chatId, message.text, detail.firstContact.id, message.mediaUri, message.mediaMimeType, detail, PushReason.IncomingMessage)
    
                    // Show notification if the chat is not on the foreground.
                    if (chatId != currentChat) {
                        try {
                            val messages = messageDao.loadAll(chatId)
                            if (messages.isNotEmpty()) {
                                notificationHelper.showNotification(
                                    detail.firstContact,
                                    messages.last(), // Get only the last message
                                    false,
                                )
                            }
                        } catch (e: Exception) {
                            // Handle notification failure
                        }
                    }
    
                    try {
                        widgetModelRepository.updateUnreadMessagesForContact(contactId = detail.firstContact.id, unread = true)
                    } catch (e: Exception) {
                        // Handle widget update failure
                    }
                } catch (e: Exception) {
                    // Handle any exceptions that might occur during message processing
                }
            }
        } catch (e: Exception) {
            // Handle any top-level exceptions
        }
    }

    /**
     * Add list of short form videos as sent messages to current chat history.This is used to test the preload manager of exoplayer
     */
    private suspend fun preloadShortVideos(
        chatId: Long,
        detail: ChatDetail,
        pushReason: PushReason,
    ) {
        try {
            for ((index, uri) in ShortsVideoList.mediaUris.withIndex())
                saveMessageAndNotify(
                    chatId,
                    "Shorts $index",
                    0L,
                    uri,
                    "video/mp4",
                    detail,
                    pushReason,
                )
        } catch (e: Exception) {
            // Handle preload videos error
        }
    }

    private suspend fun saveMessageAndNotify(
        chatId: Long,
        text: String,
        senderId: Long,
        mediaUri: String?,
        mediaMimeType: String?,
        detail: ChatDetail,
        pushReason: PushReason,
    ) {
        try {
            // Create the message.
            val message = Message.Builder().apply {
                this.chatId = chatId
                this.senderId = senderId
                this.text = text
                this.timestamp = System.currentTimeMillis()
                if (!mediaUri.isNullOrEmpty()) {
                    this.mediaUri = mediaUri
                }
                if (!mediaMimeType.isNullOrEmpty()) {
                    this.mediaMimeType = mediaMimeType
                }
            }.build()
    
            // Save it to the database.
            val messageId = try {
                messageDao.insert(message)
            } catch (e: Exception) {
                // If message can't be inserted, return and don't continue
                return
            }
    
            val contact = if (senderId == 0L) {
                try {
                    // Message from self, notify peer.
                    detail.firstContact
                } catch (e: Exception) {
                    return
                }
            } else {
                try {
                    // Message from peer, notify self.
                    contactDao.loadAll().find { it.id == 0L } ?: return
                } catch (e: Exception) {
                    return
                }
            }
    
            if (pushReason == PushReason.IncomingMessage && chatId == currentChat) {
                // The chat is on the foreground. Don't show a notification, but fetch the message from
                // database. This is just to illustrate the use of the data channel (notification button
                // generates a side effect which is observed by the app.)
            } else if (senderId != 0L) {
                // The chat is not on the foreground, and it's an incoming message. Show a notification.
                notificationHelper.showNotification(
                    contact,
                    message,
                    chatId == currentChat,
                )
            }
        } catch (e: Exception) {
            // Handle any exceptions that might occur during message saving
        }
    }

    suspend fun markAsRead(chatId: Long) {
        try {
            messageDao.markAsRead(chatId)
        } catch (e: Exception) {
            // Handle mark as read error
        }
    }

    fun setCurrentChat(chat: Long?) {
        currentChat = chat ?: 0L
    }

    suspend fun updateNotification(chatId: Long) {
        try {
            val chat = chatDao.loadDetailById(chatId) ?: return
            val messages = messageDao.loadAll(chatId)
            if (messages.isNotEmpty()) {
                notificationHelper.showNotification(
                    chat.firstContact,
                    messages.last(),
                    chatId == currentChat,
                )
            }
        } catch (e: Exception) {
            // Handle update notification error
        }
    }

    private suspend fun getMessageHistory(chatId: Long): List<Content> {
        try {
            val messages = messageDao.loadAll(chatId)
            return messages.map { message ->
                if (message.senderId == 0L) {
                    // Message from user (id = 0)
                    content { role("user"); text(message.text) }
                } else {
                    // Message from model
                    content { role("model"); text(message.text) }
                }
            }
        } catch (e: Exception) {
            // Handle message history error
            return emptyList()
        }
    }
}