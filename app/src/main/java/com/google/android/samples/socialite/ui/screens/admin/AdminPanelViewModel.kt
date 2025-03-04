package com.google.android.samples.socialite.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.samples.socialite.data.repository.MessageRepository
import com.google.android.samples.socialite.data.repository.UserRepository
import com.google.android.samples.socialite.model.QuickResponse
import com.google.android.samples.socialite.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminPanelUiState(
    val isLoading: Boolean = true,
    val quickResponses: List<QuickResponse> = emptyList(),
    val followerCount: Int = 0,
    val messageCount: Int = 0,
    val postCount: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPanelUiState())
    val uiState: StateFlow<AdminPanelUiState> = _uiState.asStateFlow()

    init {
        loadAdminData()
    }

    private fun loadAdminData() {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value

            if (currentUser == null || currentUser.role != UserRole.BAND_MEMBER) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "You do not have permission to access this area."
                )
                return@launch
            }

            // Load quick responses
            messageRepository.getQuickResponsesForBandMember(currentUser.id).collectLatest { responses ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    quickResponses = responses,
                    followerCount = currentUser.followers.size,
                    // These values would typically come from other repositories
                    messageCount = 32, // Mock value
                    postCount = 15 // Mock value
                )
            }
        }
    }

    fun addQuickResponse(category: String, content: String) {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value ?: return@launch

            if (content.isBlank()) return@launch

            messageRepository.addQuickResponse(
                bandMemberId = currentUser.id,
                content = content,
                category = category
            )
        }
    }

    fun deleteQuickResponse(responseId: String) {
        viewModelScope.launch {
            messageRepository.deleteQuickResponse(responseId)
        }
    }
}
