package com.aiagram.presentation.post.detail

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

data class PostDetailUiState(
    val post: Post? = null,
    val author: User? = null,
    val isLiked: Boolean = false,
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState

    fun loadPost(postId: String) {
        viewModelScope.launch {
            val userId = tokenDataStore.getUserId().firstOrNull() ?: ""
            _uiState.value = PostDetailUiState(isLoading = true, currentUserId = userId)
            postRepository.getPostById(postId).onSuccess { post ->
                _uiState.value = _uiState.value.copy(post = post, isLoading = false)
                userRepository.getUserById(post.userId).onSuccess { author ->
                    _uiState.value = _uiState.value.copy(author = author)
                }
                // Check likes
                postRepository.getLikes(postId, 0, 200).onSuccess { likes ->
                    _uiState.value = _uiState.value.copy(isLiked = likes.contains(userId))
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleLike() {
        val post = _uiState.value.post ?: return
        viewModelScope.launch {
            if (_uiState.value.isLiked) {
                postRepository.unlikePost(post.id).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLiked = false,
                        post = post.copy(likeCount = post.likeCount - 1)
                    )
                }
            } else {
                postRepository.likePost(post.id).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLiked = true,
                        post = post.copy(likeCount = post.likeCount + 1)
                    )
                }
            }
        }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            postRepository.deletePost(postId).onSuccess { onSuccess() }
        }
    }
}
