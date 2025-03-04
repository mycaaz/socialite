package com.google.android.samples.socialite.ui.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.samples.socialite.data.repository.UserRepository
import com.google.android.samples.socialite.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverBandMembersUiState(
    val isLoading: Boolean = true,
    val bandMembers: List<User> = emptyList(),
    val availableGenres: List<String> = emptyList(),
    val selectedGenre: String? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class DiscoverBandMembersViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverBandMembersUiState())
    val uiState: StateFlow<DiscoverBandMembersUiState> = _uiState.asStateFlow()

    private var allBandMembers: List<User> = emptyList()

    init {
        loadBandMembers()
    }

    private fun loadBandMembers() {
        viewModelScope.launch {
            userRepository.getBandMembers().collectLatest { bandMembers ->
                allBandMembers = bandMembers

                // Extract all available genres from band members
                val genres = bandMembers
                    .flatMap { it.bandInfo?.genres ?: emptyList() }
                    .distinct()
                    .sorted()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bandMembers = filterBandMembers(
                        bandMembers = bandMembers,
                        genre = _uiState.value.selectedGenre,
                        query = _uiState.value.searchQuery
                    ),
                    availableGenres = genres
                )
            }
        }
    }

    fun filterByGenre(genre: String?) {
        _uiState.value = _uiState.value.copy(
            selectedGenre = genre,
            bandMembers = filterBandMembers(
                bandMembers = allBandMembers,
                genre = genre,
                query = _uiState.value.searchQuery
            )
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            bandMembers = filterBandMembers(
                bandMembers = allBandMembers,
                genre = _uiState.value.selectedGenre,
                query = query
            )
        )
    }

    private fun filterBandMembers(
        bandMembers: List<User>,
        genre: String?,
        query: String
    ): List<User> {
        return bandMembers
            .filter { user ->
                // Filter by genre if selected
                (genre == null || user.bandInfo?.genres?.contains(genre) == true) &&
                    // Filter by search query if any
                    (query.isEmpty() ||
                        user.name.contains(query, ignoreCase = true) ||
                        user.bandInfo?.bandName?.contains(query, ignoreCase = true) == true ||
                        user.bandInfo?.role?.contains(query, ignoreCase = true) == true ||
                        user.bandInfo?.instruments?.any { it.contains(query, ignoreCase = true) } == true)
            }
    }
}
