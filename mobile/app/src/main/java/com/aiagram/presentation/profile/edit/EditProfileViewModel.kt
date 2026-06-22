package com.aiagram.presentation.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagram.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val bio: String = "",
    val avatarUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            userRepository.getMyProfile().onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    bio = user.bio ?: "",
                    avatarUrl = user.avatarUrl ?: ""
                )
            }
        }
    }

    fun onBioChange(v: String) { _uiState.value = _uiState.value.copy(bio = v) }
    fun onAvatarUrlChange(v: String) { _uiState.value = _uiState.value.copy(avatarUrl = v) }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.updateMyProfile(
                _uiState.value.bio.ifBlank { null },
                _uiState.value.avatarUrl.ifBlank { null }
            )
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
}
