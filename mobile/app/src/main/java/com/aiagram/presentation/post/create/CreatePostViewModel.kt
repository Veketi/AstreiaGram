package com.aiagram.presentation.post.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagram.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePostUiState(
    val imageUrl: String = "",
    val caption: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState

    fun onImageUrlChange(v: String) { _uiState.value = _uiState.value.copy(imageUrl = v, error = null) }
    fun onCaptionChange(v: String) { _uiState.value = _uiState.value.copy(caption = v) }

    fun createPost() {
        val state = _uiState.value
        if (state.imageUrl.isBlank()) {
            _uiState.value = state.copy(error = "A URL da imagem é obrigatória")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = postRepository.createPost(state.imageUrl, state.caption)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Erro ao criar post")
            }
        }
    }
}
