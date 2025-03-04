package com.google.android.samples.socialite.ui.screens.chat

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.model.Location
import com.google.android.samples.socialite.model.Message
import com.google.android.samples.socialite.model.MessagePriority
import com.google.android.samples.socialite.model.User
import com.google.android.samples.socialite.ui.components.LoadingScreen
import com.google.android.samples.socialite.ui.theme.BandConnectTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageScreen(
    viewModel: MessageViewModel = hiltViewModel(),
    recipientId: String,
    onBackClick: () -> Unit,
    onShareLocationClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = recipientId) {
        viewModel.initConversation(recipientId)
    }

    Scaffold(
        topBar = {
            MessageTopBar(
                recipientName = uiState.recipient?.name ?: "Loading...",
                isOnline = uiState.isRecipientOnline,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            MessageContent(
                messages = uiState.messages,
                currentUserId = uiState.currentUserId,
                onSendMessage = { message, location ->
                    viewModel.sendMessage(message, location)
                },
                onShareLocationClick = onShareLocationClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun MessageTopBar(
    recipientName: String,
    isOnline: Boolean,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = recipientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isOnline) stringResource(id = R.string.online) else stringResource(id = R.string.offline),
                    fontSize = 12.sp,
                    color = if (isOnline) Color.Green else Color.Gray
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        backgroundColor = MaterialTheme.colors.primary
    )
}

@Composable
fun MessageContent(
    messages: List<Message>,
    currentUserId: String,
    onSendMessage: (String, Location?) -> Unit,
    onShareLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }
    var attachedLocation by remember { mutableStateOf<Location?>(null) }

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.scrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_messages),
                            fontSize = 16.sp,
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(messages) { message ->
                    ChatMessageItem(
                        message = message,
                        isOwnMessage = message.senderId == currentUserId
                    )
                }
            }
        }

        // Message Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 8.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Show attached location if any
                if (attachedLocation != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colors.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = attachedLocation!!.locationName,
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(
                                onClick = { attachedLocation = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Message text field
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text(stringResource(id = R.string.message_hint)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Location button
                    IconButton(
                        onClick = {
                            // In a real implementation, this would get the current location
                            // For now, we'll just create a mock location
                            attachedLocation = Location(
                                latitude = 37.7749,
                                longitude = -122.4194,
                                locationName = "San Francisco, CA"
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Add Location",
                            tint = MaterialTheme.colors.primary
                        )
                    }

                    // Send button
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText, attachedLocation)
                                messageText = ""
                                attachedLocation = null
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: Message,
    isOwnMessage: Boolean
) {
    val configuration = LocalConfiguration.current
    val maxWidth = (configuration.screenWidthDp * 0.75).dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        // Message bubble
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isOwnMessage) {
                        when (message.priority) {
                            MessagePriority.HIGH -> MaterialTheme.colors.primary
                            MessagePriority.URGENT -> Color(0xFFE53935)
                            else -> MaterialTheme.colors.primary.copy(alpha = 0.8f)
                        }
                    } else {
                        MaterialTheme.colors.surface
                    }
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = if (isOwnMessage) Color.White else MaterialTheme.colors.onSurface
                )

                // Display location if attached
                message.attachedLocation?.let { location ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = if (isOwnMessage) Color.White else MaterialTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = location.locationName,
                            fontSize = 12.sp,
                            color = if (isOwnMessage) Color.White.copy(alpha = 0.8f) else MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Timestamp
        Text(
            text = formatTimestamp(message.timestamp),
            fontSize = 10.sp,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun ChatMessageItemPreview() {
    val message = Message(
        senderId = "user1",
        receiverId = "user2",
        content = "Hey there! I really enjoyed your concert last week. Any chance you'll be coming back to our city soon?",
        timestamp = System.currentTimeMillis(),
        attachedLocation = Location(
            latitude = 37.7749,
            longitude = -122.4194,
            locationName = "San Francisco, CA"
        )
    )

    BandConnectTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Own message
            ChatMessageItem(
                message = message,
                isOwnMessage = true
            )

            // Other person's message
            ChatMessageItem(
                message = message.copy(content = "Thanks! Yes, we'll be back in June. Looking forward to seeing you again!"),
                isOwnMessage = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageContentPreview() {
    val messages = listOf(
        Message(
            senderId = "user1",
            receiverId = "user2",
            content = "Hey there! I really enjoyed your concert last week.",
            timestamp = System.currentTimeMillis() - 3600000
        ),
        Message(
            senderId = "user2",
            receiverId = "user1",
            content = "Thanks! I'm glad you enjoyed it. We had a blast performing for everyone.",
            timestamp = System.currentTimeMillis() - 3000000
        ),
        Message(
            senderId = "user1",
            receiverId = "user2",
            content = "Any chance you'll be coming back to our city soon?",
            timestamp = System.currentTimeMillis() - 2400000
        ),
        Message(
            senderId = "user2",
            receiverId = "user1",
            content = "Yes, we're planning to return in June. Keep an eye on our updates!",
            timestamp = System.currentTimeMillis() - 1800000,
            attachedLocation = Location(
                latitude = 37.7749,
                longitude = -122.4194,
                locationName = "San Francisco, CA"
            )
        )
    )

    BandConnectTheme {
        MessageContent(
            messages = messages,
            currentUserId = "user1",
            onSendMessage = { _, _ -> },
            onShareLocationClick = {}
        )
    }
}
