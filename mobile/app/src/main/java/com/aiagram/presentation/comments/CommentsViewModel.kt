package com.aiagram.presentation.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagram.data.local.TokenDataStore
import com.aiagram.domain.model.Comment
import com.aiagram.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.aiagram.domain.model.User
import com.aiagram.domain.repository.UserRepository

data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val authors: Map<String, User> = emptyMap(),
    val newComment: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val currentUserId: String = ""
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState

    fun loadComments(postId: String) {
        viewModelScope.launch {
            val userId = tokenDataStore.getUserId().firstOrNull() ?: ""
            _uiState.value = _uiState.value.copy(isLoading = true, currentUserId = userId)
            postRepository.getComments(postId, 0, 50).onSuccess { comments ->
                val authorIds = comments.map { it.userId }.distinct()
                val authorsMap = _uiState.value.authors.toMutableMap()
                authorIds.forEach { uid ->
                    if (!authorsMap.containsKey(uid)) {
                        userRepository.getUserById(uid).onSuccess { user ->
                            authorsMap[uid] = user
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(
                    comments = comments,
                    authors = authorsMap,
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onCommentChange(v: String) { _uiState.value = _uiState.value.copy(newComment = v) }

    fun addComment(postId: String) {
        val content = _uiState.value.newComment.trim()
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            postRepository.addComment(postId, content).onSuccess {
                _uiState.value = _uiState.value.copy(newComment = "", isSending = false)
                loadComments(postId) // This will refresh the comments list smoothly
            }.onFailure {
                _uiState.value = _uiState.value.copy(isSending = false)
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            postRepository.deleteComment(postId, commentId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    comments = _uiState.value.comments.filter { it.id != commentId }
                )
            }
        }
    }
}
