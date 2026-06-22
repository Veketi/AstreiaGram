package com.aiagram.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagram.data.local.TokenDataStore
import com.aiagram.domain.model.Post
import com.aiagram.domain.model.User
import com.aiagram.domain.repository.FeedRepository
import com.aiagram.domain.repository.PostRepository
import com.aiagram.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val authorCache: Map<String, User> = emptyMap(),
    val likedPostIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentUserId: String = "",
    val currentPage: Int = 1,
    val hasMore: Boolean = true
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState

    init {
        viewModelScope.launch {
            val userId = tokenDataStore.getUserId().firstOrNull() ?: ""
            _uiState.value = _uiState.value.copy(currentUserId = userId)
            loadFeed(refresh = true)
        }
    }

    fun loadFeed(refresh: Boolean = false) {
        val state = _uiState.value
        if (state.isLoading) return
        if (refresh) {
            _uiState.value = state.copy(isRefreshing = true, currentPage = 1, hasMore = true)
        } else {
            if (!state.hasMore) return
            _uiState.value = state.copy(isLoading = true)
        }

        viewModelScope.launch {
            val userId = _uiState.value.currentUserId
            val page = if (refresh) 1 else _uiState.value.currentPage
            val result = feedRepository.getFeed(userId, page, 20)
            result.onSuccess { feed ->
                val newPosts = if (refresh) feed.posts else _uiState.value.posts + feed.posts
                _uiState.value = _uiState.value.copy(
                    posts = newPosts,
                    isLoading = false,
                    isRefreshing = false,
                    currentPage = page + 1,
                    hasMore = feed.posts.size >= 20,
                    error = null
                )
                // Pre-fetch unique authors
                val authorIds = feed.posts.map { it.userId }.distinct()
                    .filter { !_uiState.value.authorCache.containsKey(it) }
                authorIds.forEach { uid ->
                    userRepository.getUserById(uid).onSuccess { user ->
                        _uiState.value = _uiState.value.copy(
                            authorCache = _uiState.value.authorCache + (uid to user)
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false, isRefreshing = false,
                    error = e.message ?: "Erro ao carregar feed"
                )
            }
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            val isLiked = _uiState.value.likedPostIds.contains(post.id)
            if (isLiked) {
                postRepository.unlikePost(post.id).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        likedPostIds = _uiState.value.likedPostIds - post.id,
                        posts = _uiState.value.posts.map {
                            if (it.id == post.id) it.copy(likeCount = it.likeCount - 1) else it
                        }
                    )
                }
            } else {
                postRepository.likePost(post.id).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        likedPostIds = _uiState.value.likedPostIds + post.id,
                        posts = _uiState.value.posts.map {
                            if (it.id == post.id) it.copy(likeCount = it.likeCount + 1) else it
                        }
                    )
                }
            }
        }
    }
}
