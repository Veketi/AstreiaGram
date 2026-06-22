package com.aiagram.presentation.profile.followlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagram.domain.model.UserSummary
import com.aiagram.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowListUiState(
    val users: List<UserSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FollowListViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowListUiState())
    val uiState: StateFlow<FollowListUiState> = _uiState

    fun load(userId: String, type: String) {
        viewModelScope.launch {
            _uiState.value = FollowListUiState(isLoading = true)
            val result = if (type == "followers") {
                userRepository.getFollowers(userId)
            } else {
                userRepository.getFollowing(userId)
            }
            result.onSuccess { users ->
                _uiState.value = FollowListUiState(users = users)
            }.onFailure { e ->
                _uiState.value = FollowListUiState(error = e.message)
            }
        }
    }
}
