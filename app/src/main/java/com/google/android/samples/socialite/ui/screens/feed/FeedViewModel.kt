package com.google.android.samples.socialite.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.samples.socialite.data.repository.PostRepository
import com.google.android.samples.socialite.data.repository.UserRepository
import com.google.android.samples.socialite.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val isLoading: Boolean = true,
    val posts: List<Post> = emptyList(),
    val userMap: Map<String, String> = emptyMap(), // Map of user IDs to names
    val likedPosts: Set<String> = emptySet(),
    val errorMessage: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value

            // Combine users and posts flows
            combine(
                userRepository.users,
                postRepository.getPublicPosts()
            ) { users, posts ->
                val userMap = users.associate { it.id to it.name }
                val likedPosts = if (currentUser != null) {
                    posts.filter { post ->
                        post.likes.contains(currentUser.id)
                    }.map { it.id }.toSet()
                } else {
                    emptySet()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    posts = posts,
                    userMap = userMap,
                    likedPosts = likedPosts
                )
            }.collect { /* Just collecting the combined flow */ }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value ?: return@launch

            if (_uiState.value.likedPosts.contains(postId)) {
                // Unlike
                postRepository.unlikePost(postId, currentUser.id)
                _uiState.value = _uiState.value.copy(
                    likedPosts = _uiState.value.likedPosts - postId
                )
            } else {
                // Like
                postRepository.likePost(postId, currentUser.id)
                _uiState.value = _uiState.value.copy(
                    likedPosts = _uiState.value.likedPosts + postId
                )
            }
        }
    }
}
