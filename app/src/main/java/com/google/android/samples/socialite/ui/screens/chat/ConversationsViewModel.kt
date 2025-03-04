package com.google.android.samples.socialite.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.samples.socialite.data.repository.MessageRepository
import com.google.android.samples.socialite.data.repository.UserRepository
import com.google.android.samples.socialite.model.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationsUiState(
    val isLoading: Boolean = true,
    val conversations: List<Conversation> = emptyList(),
    val userMap: Map<String, String> = emptyMap(), // Map of user IDs to names
    val currentUserId: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "You need to be logged in to view conversations."
                )
                return@launch
            }

            // Combine users and conversations flows
            combine(
                userRepository.users,
                messageRepository.getConversationsForUser(currentUser.id)
            ) { users, conversations ->
                val userMap = users.associate { it.id to it.name }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    conversations = conversations,
                    userMap = userMap,
                    currentUserId = currentUser.id
                )
            }.collectLatest { /* Just collecting the combined flow */ }
        }
    }

    fun pinConversation(conversationId: String, isPinned: Boolean) {
        viewModelScope.launch {
            messageRepository.pinConversation(conversationId, isPinned)
        }
    }
}
