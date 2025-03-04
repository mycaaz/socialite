package com.google.android.samples.socialite.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.samples.socialite.data.repository.MessageRepository
import com.google.android.samples.socialite.data.repository.UserRepository
import com.google.android.samples.socialite.model.Location
import com.google.android.samples.socialite.model.Message
import com.google.android.samples.socialite.model.MessagePriority
import com.google.android.samples.socialite.model.User
import com.google.android.samples.socialite.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageUiState(
    val isLoading: Boolean = true,
    val recipient: User? = null,
    val messages: List<Message> = emptyList(),
    val currentUserId: String = "",
    val isRecipientOnline: Boolean = false,
    val quickResponses: List<String> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipientId: String = checkNotNull(savedStateHandle["recipientId"])

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    fun initConversation(recipientId: String) {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "You need to be logged in to send messages."
                )
                return@launch
            }

            // Load recipient info
            userRepository.getBandMemberById(recipientId).collectLatest { recipient ->
                if (recipient != null) {
                    _uiState.value = _uiState.value.copy(
                        recipient = recipient,
                        isRecipientOnline = isUserOnline(recipient),
                        currentUserId = currentUser.id
                    )

                    // Load messages
                    loadMessages(currentUser.id, recipientId)

                    // Load quick responses if user is a band member
                    if (currentUser.role == UserRole.BAND_MEMBER) {
                        loadQuickResponses(currentUser.id)
                    }

                    _uiState.value = _uiState.value.copy(isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Recipient not found."
                    )
                }
            }
        }
    }

    private fun loadMessages(currentUserId: String, recipientId: String) {
        viewModelScope.launch {
            messageRepository.getMessagesWithUser(currentUserId, recipientId).collectLatest { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)

                // Mark messages as read
                messages.forEach { message ->
                    if (message.receiverId == currentUserId) {
                        messageRepository.markMessageAsRead(message.id)
                    }
                }
            }
        }
    }

    private fun loadQuickResponses(bandMemberId: String) {
        viewModelScope.launch {
            messageRepository.getQuickResponsesForBandMember(bandMemberId).collectLatest { responses ->
                _uiState.value = _uiState.value.copy(
                    quickResponses = responses.map { it.content }
                )
            }
        }
    }

    fun sendMessage(content: String, location: Location? = null) {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value ?: return@launch
            val recipientId = _uiState.value.recipient?.id ?: return@launch

            // Determine if this is a priority message (for band members)
            val priority = if (currentUser.role == UserRole.BAND_MEMBER) {
                MessagePriority.HIGH
            } else {
                MessagePriority.NORMAL
            }

            messageRepository.sendMessage(
                senderId = currentUser.id,
                receiverId = recipientId,
                content = content,
                location = location,
                priority = priority
            )
        }
    }

    fun sendQuickResponse(responseIndex: Int) {
        viewModelScope.launch {
            val quickResponses = _uiState.value.quickResponses
            if (responseIndex < quickResponses.size) {
                sendMessage(quickResponses[responseIndex], null)
            }
        }
    }

    // In a real app, this would check the user's actual online status
    private fun isUserOnline(user: User): Boolean {
        val lastActive = user.lastActive
        val currentTime = System.currentTimeMillis()
        // Consider a user online if they were active in the last 5 minutes
        return (currentTime - lastActive) < 5 * 60 * 1000
    }
}
