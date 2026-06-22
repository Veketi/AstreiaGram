package com.aiagram.presentation.profile.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagram.data.local.TokenDataStore
import com.aiagram.domain.model.Post
import com.aiagram.domain.model.User
import com.aiagram.domain.repository.PostRepository
import com.aiagram.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val userId = tokenDataStore.getUserId().firstOrNull() ?: ""
            _uiState.value = ProfileUiState(isLoading = true, currentUserId = userId)
            userRepository.getMyProfile().onSuccess { user ->
                _uiState.value = _uiState.value.copy(user = user, isLoading = false)
                postRepository.getPostsByUserId(userId, 0, 30).onSuccess { posts ->
                    _uiState.value = _uiState.value.copy(posts = posts)
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            tokenDataStore.clearAuthData()
            onComplete()
        }
    }
}
