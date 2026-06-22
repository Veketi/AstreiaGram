package com.aiagram.presentation.profile.user

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

data class UserProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val isFollowing: Boolean = false,
    val isLoading: Boolean = false,
    val isFollowLoading: Boolean = false,
    val currentUserId: String = "",
    val error: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState

    fun loadUser(userId: String) {
        viewModelScope.launch {
            val currentId = tokenDataStore.getUserId().firstOrNull() ?: ""
            _uiState.value = UserProfileUiState(isLoading = true, currentUserId = currentId)
            userRepository.getUserById(userId).onSuccess { user ->
                _uiState.value = _uiState.value.copy(user = user, isLoading = false)
                // Check if following
                userRepository.getFollowers(userId).onSuccess { followers ->
                    _uiState.value = _uiState.value.copy(
                        isFollowing = followers.any { it.id == currentId }
                    )
                }
                // Load posts
                postRepository.getPostsByUserId(userId, 0, 30).onSuccess { posts ->
                    _uiState.value = _uiState.value.copy(posts = posts)
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleFollow(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFollowLoading = true)
            if (_uiState.value.isFollowing) {
                userRepository.unfollowUser(userId).onSuccess {
                    val user = _uiState.value.user
                    _uiState.value = _uiState.value.copy(
                        isFollowing = false,
                        isFollowLoading = false,
                        user = user?.copy(followerCount = user.followerCount - 1)
                    )
                }
            } else {
                userRepository.followUser(userId).onSuccess {
                    val user = _uiState.value.user
                    _uiState.value = _uiState.value.copy(
                        isFollowing = true,
                        isFollowLoading = false,
                        user = user?.copy(followerCount = user.followerCount + 1)
                    )
                }
            }
        }
    }
}
