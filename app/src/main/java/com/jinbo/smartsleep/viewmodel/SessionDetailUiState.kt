package com.jinbo.smartsleep.viewmodel

import com.jinbo.smartsleep.data.database.SessionEntity

/**
 * UI State for Session Detail screen
 */
sealed class SessionDetailUiState {
    object Loading : SessionDetailUiState()
    data class Success(val session: SessionEntity) : SessionDetailUiState()
    data class Error(val message: String) : SessionDetailUiState()
}
