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
 * ViewModel for History screen
 */
class HistoryViewModel(
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadAllSessions()
    }

    /**
     * Load all sessions
     */
    fun loadAllSessions() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            try {
                repository.getAllSessions().collect { sessions ->
                    _uiState.value = HistoryUiState.Success(sessions)
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(
                    e.message ?: "Failed to load history"
                )
            }
        }
    }

    companion object {
        fun provideFactory(repository: SessionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                        return HistoryViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
                }
            }
    }
}

/**
 * UI State for History screen
 */
sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val sessions: List<SessionEntity>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}
