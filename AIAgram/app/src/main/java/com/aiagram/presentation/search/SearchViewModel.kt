package com.aiagram.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagram.domain.model.User
import com.aiagram.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val result: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val notFound: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    fun onQueryChange(v: String) {
        _uiState.value = _uiState.value.copy(query = v, error = null, notFound = false, result = null)
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, notFound = false, result = null)
            val isUuid = query.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
            val searchRequest = if (isUuid) userRepository.getUserById(query) else userRepository.getUserByUsername(query)
            
            searchRequest.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false, result = user)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, notFound = true)
            }
        }
    }
}
