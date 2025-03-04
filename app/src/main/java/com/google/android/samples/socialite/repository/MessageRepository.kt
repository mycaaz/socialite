package com.google.android.samples.socialite.data.repository

import com.google.android.samples.socialite.model.Conversation
import com.google.android.samples.socialite.model.Location
import com.google.android.samples.socialite.model.Message
import com.google.android.samples.socialite.model.MessagePriority
import com.google.android.samples.socialite.model.MessageStatus
import com.google.android.samples.socialite.model.QuickResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor() {

    // Mock quick responses
    private val sampleQuickResponses = listOf(
        QuickResponse(
            bandMemberId = "user1", // John Lennon
            content = "Thanks for reaching out! I appreciate your support.",
            category = "General"
        ),
        QuickResponse(
            bandMemberId = "user1",
            content = "We'll be in your city soon! Keep an eye on our tour dates.",
            category = "Events"
        ),
        QuickResponse(
            bandMemberId = "user2", // Paul McCartney
            content = "I'm glad you enjoyed our latest album!",
            category = "Music"
        ),
        QuickResponse(
            bandMemberId = "user3", // Dave Grohl
            content = "That's awesome to hear! Rock on!",
            category = "Feedback"
        ),
        QuickResponse(
            bandMemberId = "user4", // Taylor Swift
            content = "I'm working on new music right now - can't wait to share it!",
            category = "Updates"
        )
    )

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val _quickResponses = MutableStateFlow<List<QuickResponse>>(sampleQuickResponses)

    val messages = _messages.asStateFlow()
    val conversations = _conversations.asStateFlow()
    val quickResponses = _quickResponses.asStateFlow()

    fun getConversationsForUser(userId: String): Flow<List<Conversation>> {
        return _conversations.map { convos ->
            convos.filter { it.participants.contains(userId) }
                .sortedWith(compareByDescending<Conversation> { it.isPinned }
                    .thenByDescending { it.lastUpdated })
        }
    }

    fun getMessagesForConversation(conversationId: String): Flow<List<Message>> {
        return _messages.map { msgs ->
            msgs.filter { msg ->
                _conversations.value.firstOrNull { it.id == conversationId }?.participants?.let { participants ->
                    participants.contains(msg.senderId) && participants.contains(msg.receiverId)
                } ?: false
            }.sortedBy { it.timestamp }
        }
    }

    fun getMessagesWithUser(userId: String, otherUserId: String): Flow<List<Message>> {
        return _messages.map { msgs ->
            msgs.filter { msg ->
                (msg.senderId == userId && msg.receiverId == otherUserId) ||
                    (msg.senderId == otherUserId && msg.receiverId == userId)
            }.sortedBy { it.timestamp }
        }
    }

    fun getQuickResponsesForBandMember(bandMemberId: String): Flow<List<QuickResponse>> {
        return _quickResponses.map { responses ->
            responses.filter { it.bandMemberId == bandMemberId }
        }
    }

    suspend fun sendMessage(
        senderId: String,
        receiverId: String,
        content: String,
        location: Location? = null,
        imageUrl: String? = null,
        priority: MessagePriority = MessagePriority.NORMAL,
        isQuickResponse: Boolean = false
    ): Message {
        val message = Message(
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            attachedLocation = location,
            imageUrl = imageUrl,
            priority = priority,
            isQuickResponse = isQuickResponse
        )

        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages

        // Update or create conversation
        updateConversation(senderId, receiverId, message)

        return message
    }

    private suspend fun updateConversation(senderId: String, receiverId: String, message: Message) {
        val participants = listOf(senderId, receiverId).sorted()

        val conversationList = _conversations.value.toMutableList()
        val existingConvoIndex = conversationList.indexOfFirst { convo ->
            convo.participants.containsAll(participants) && convo.participants.size == participants.size
        }

        if (existingConvoIndex >= 0) {
            // Update existing conversation
            val currentConvo = conversationList[existingConvoIndex]
            val updatedUnreadCount = if (currentConvo.participants[0] == message.receiverId) {
                currentConvo.unreadCount + 1
            } else {
                currentConvo.unreadCount
            }

            val updatedConvo = currentConvo.copy(
                lastMessage = message,
                lastUpdated = message.timestamp,
                unreadCount = updatedUnreadCount
            )
            conversationList[existingConvoIndex] = updatedConvo
        } else {
            // Create new conversation
            val newConvo = Conversation(
                participants = participants,
                lastMessage = message,
                lastUpdated = message.timestamp,
                unreadCount = if (participants[0] == message.receiverId) 1 else 0
            )
            conversationList.add(newConvo)
        }

        _conversations.value = conversationList
    }

    suspend fun markMessageAsRead(messageId: String) {
        val updatedMessages = _messages.value.map { msg ->
            if (msg.id == messageId) msg.copy(status = MessageStatus.READ) else msg
        }
        _messages.value = updatedMessages.toList()
    }

    suspend fun markAllMessagesAsRead(conversationId: String, userId: String) {
        // First update messages
        val updatedMessages = _messages.value.map { msg ->
            if (msg.receiverId == userId) {
                msg.copy(status = MessageStatus.READ)
            } else {
                msg
            }
        }
        _messages.value = updatedMessages.toList()

        // Then update conversation unread count
        val conversationList = _conversations.value.toMutableList()
        val convoIndex = conversationList.indexOfFirst { it.id == conversationId }

        if (convoIndex >= 0) {
            val updatedConvo = conversationList[convoIndex].copy(unreadCount = 0)
            conversationList[convoIndex] = updatedConvo
            _conversations.value = conversationList
        }
    }

    suspend fun addQuickResponse(bandMemberId: String, content: String, category: String): QuickResponse {
        val quickResponse = QuickResponse(
            bandMemberId = bandMemberId,
            content = content,
            category = category
        )

        val currentResponses = _quickResponses.value.toMutableList()
        currentResponses.add(quickResponse)
        _quickResponses.value = currentResponses

        return quickResponse
    }

    suspend fun deleteQuickResponse(responseId: String) {
        val updatedResponses = _quickResponses.value.filterNot { it.id == responseId }
        _quickResponses.value = updatedResponses
    }

    suspend fun pinConversation(conversationId: String, isPinned: Boolean) {
        val updatedConversations = _conversations.value.map { convo ->
            if (convo.id == conversationId) convo.copy(isPinned = isPinned) else convo
        }
        _conversations.value = updatedConversations.toList()
    }
}
```
