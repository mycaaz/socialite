package com.google.android.samples.socialite.model

import java.util.UUID

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

enum class MessagePriority {
    NORMAL,
    HIGH,
    URGENT
}

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val attachedLocation: Location? = null,
    val imageUrl: String? = null,
    val priority: MessagePriority = MessagePriority.NORMAL,
    val isQuickResponse: Boolean = false
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val participants: List<String>,
    val lastMessage: Message? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false
)

data class QuickResponse(
    val id: String = UUID.randomUUID().toString(),
    val bandMemberId: String,
    val content: String,
    val category: String
)
