package com.jinbo.smartsleep.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jinbo.smartsleep.data.SessionRepository
import com.jinbo.smartsleep.data.database.SessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Session Detail screen
 */
class SessionDetailViewModel(
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionDetailUiState>(SessionDetailUiState.Loading)
    val uiState: StateFlow<SessionDetailUiState> = _uiState.asStateFlow()

    /**
     * Load session details
     */
    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            _uiState.value = SessionDetailUiState.Loading

            try {
                if (sessionId == 0L) {
                    _uiState.value = SessionDetailUiState.Error("Invalid session ID")
                    return@launch
                }

                val session = repository.getSessionById(sessionId)
                if (session != null) {
                    _uiState.value = SessionDetailUiState.Success(session)
                } else {
                    _uiState.value = SessionDetailUiState.Error("Session not found")
                }
            } catch (e: Exception) {
                _uiState.value = SessionDetailUiState.Error(
                    e.message ?: "Failed to load session"
                )
            }
        }
    }

    companion object {
        fun provideFactory(repository: SessionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SessionDetailViewModel::class.java)) {
                        return SessionDetailViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
                }
            }
    }
}
