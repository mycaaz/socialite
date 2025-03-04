package com.google.android.samples.socialite.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.samples.socialite.data.repository.PostRepository
import com.google.android.samples.socialite.data.repository.UserRepository
import com.google.android.samples.socialite.model.Post
import com.google.android.samples.socialite.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BandMemberProfileUiState(
    val isLoading: Boolean = true,
    val bandMember: User? = null,
    val isFollowing: Boolean = false,
    val posts: List<Post> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class BandMemberProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bandMemberId: String = checkNotNull(savedStateHandle["bandMemberId"])

    private val _uiState = MutableStateFlow(BandMemberProfileUiState())
    val uiState: StateFlow<BandMemberProfileUiState> = _uiState.asStateFlow()

    init {
        loadBandMemberProfile()
        loadBandMemberPosts()
    }

    private fun loadBandMemberProfile() {
        viewModelScope.launch {
            userRepository.getBandMemberById(bandMemberId).collectLatest { bandMember ->
                if (bandMember != null) {
                    val currentUser = userRepository.currentUser.value
                    val isFollowing = currentUser?.following?.contains(bandMemberId) ?: false

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bandMember = bandMember,
                        isFollowing = isFollowing
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Band member not found"
                    )
                }
            }
        }
    }

    private fun loadBandMemberPosts() {
        viewModelScope.launch {
            postRepository.getPostsByAuthor(bandMemberId).collectLatest { posts ->
                _uiState.value = _uiState.value.copy(
                    posts = posts
                )
            }
        }
    }

    fun toggleFollow(bandMemberId: String) {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value ?: return@launch

            if (_uiState.value.isFollowing) {
                userRepository.unfollowBandMember(currentUser.id, bandMemberId)
                _uiState.value = _uiState.value.copy(isFollowing = false)
            } else {
                userRepository.followBandMember(currentUser.id, bandMemberId)
                _uiState.value = _uiState.value.copy(isFollowing = true)
            }
        }
    }

    fun updateBandMemberLocation(latitude: Double, longitude: Double, locationName: String) {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value ?: return@launch

            if (currentUser.id == bandMemberId) {
                val location = com.google.android.samples.socialite.model.Location(
                    latitude = latitude,
                    longitude = longitude,
                    locationName = locationName
                )
                userRepository.updateUserLocation(currentUser.id, location)
            }
        }
    }
}
