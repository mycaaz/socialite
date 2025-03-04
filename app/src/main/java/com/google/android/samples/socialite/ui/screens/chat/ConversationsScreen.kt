package com.google.android.samples.socialite.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Pin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.model.Conversation
import com.google.android.samples.socialite.model.Message
import com.google.android.samples.socialite.model.MessageStatus
import com.google.android.samples.socialite.ui.components.LoadingScreen
import com.google.android.samples.socialite.ui.theme.BandConnectTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsScreen(
    viewModel: ConversationsViewModel = hiltViewModel(),
    onConversationClick: (String) -> Unit,
    onNewMessageClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.conversations)) },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewMessageClick,
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.new_message),
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            ConversationsContent(
                conversations = uiState.conversations,
                userMap = uiState.userMap,
                currentUserId = uiState.currentUserId,
                onConversationClick = onConversationClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun ConversationsContent(
    conversations: List<Conversation>,
    userMap: Map<String, String>, // Map of user IDs to names
    currentUserId: String,
    onConversationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        if (conversations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.no_conversations),
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(conversations) { conversation ->
                    val otherParticipantId = conversation.participants.firstOrNull { it != currentUserId } ?: ""
                    val otherParticipantName = userMap[otherParticipantId] ?: "Unknown User"

                    ConversationItem(
                        conversation = conversation,
                        participantName = otherParticipantName,
                        onConversationClick = { onConversationClick(otherParticipantId) }
                    )

                    Divider(
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    participantName: String,
    onConversationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onConversationClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image (placeholder)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participantName.first().toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = participantName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground
                    )

                    if (conversation.isPinned) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Pin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = formatConversationTimestamp(conversation.lastUpdated),
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Last message preview
                conversation.lastMessage?.let { lastMessage ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lastMessage.content,
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (conversation.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conversation.unreadCount.toString(),
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatConversationTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
        diff < 7 * 24 * 60 * 60 * 1000 -> {
            val format = SimpleDateFormat("EEE", Locale.getDefault())
            format.format(Date(timestamp))
        }
        else -> {
            val format = SimpleDateFormat("MM/dd", Locale.getDefault())
            format.format(Date(timestamp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationItemPreview() {
    val conversation = Conversation(
        participants = listOf("user1", "user2"),
        lastMessage = Message(
            senderId = "user2",
            receiverId = "user1",
            content = "Hey, when is your next show? I'd love to come see you perform!",
            timestamp = System.currentTimeMillis() - 30 * 60 * 1000,
            status = MessageStatus.DELIVERED
        ),
        unreadCount = 1,
        isPinned = true
    )

    BandConnectTheme {
        ConversationItem(
            conversation = conversation,
            participantName = "John Lennon",
            onConversationClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationsContentPreview() {
    val conversations = listOf(
        Conversation(
            participants = listOf("user1", "user2"),
            lastMessage = Message(
                senderId = "user2",
                receiverId = "user1",
                content = "Hey, when is your next show? I'd love to come see you perform!",
                timestamp = System.currentTimeMillis() - 30 * 60 * 1000,
                status = MessageStatus.DELIVERED
            ),
            unreadCount = 1,
            isPinned = true
        ),
        Conversation(
            participants = listOf("user1", "user3"),
            lastMessage = Message(
                senderId = "user1",
                receiverId = "user3",
                content = "Thanks for coming to our show last week!",
                timestamp = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
                status = MessageStatus.READ
            ),
            unreadCount = 0,
            isPinned = false
        )
    )

    val userMap = mapOf(
        "user2" to "John Lennon",
        "user3" to "Dave Grohl"
    )

    BandConnectTheme {
        ConversationsContent(
            conversations = conversations,
            userMap = userMap,
            currentUserId = "user1",
            onConversationClick = {}
        )
    }
}
