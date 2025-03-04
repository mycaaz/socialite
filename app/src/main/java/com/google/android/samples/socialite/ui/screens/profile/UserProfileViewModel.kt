package com.google.android.samples.socialite.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.samples.socialite.data.repository.UserRepository
import com.google.android.samples.socialite.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.currentUser.collectLatest { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = user
                )
            }
        }
    }

    fun updateNotificationSettings(enabled: Boolean) {
        viewModelScope.launch {
            val currentUser = uiState.value.currentUser ?: return@launch

            val updatedUser = currentUser.copy(
                notificationsEnabled = enabled
            )

            userRepository.updateUserProfile(currentUser.id, updatedUser)
        }
    }

    fun updateLocationSharingSettings(enabled: Boolean) {
        viewModelScope.launch {
            val currentUser = uiState.value.currentUser ?: return@launch

            val updatedUser = currentUser.copy(
                locationSharingEnabled = enabled
            )

            userRepository.updateUserProfile(currentUser.id, updatedUser)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}
