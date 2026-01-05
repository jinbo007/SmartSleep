package com.jinbo.smartsleep.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jinbo.smartsleep.data.SessionRepository
import com.jinbo.smartsleep.data.TimePeriod
import com.jinbo.smartsleep.data.database.AggregateStats
import com.jinbo.smartsleep.data.database.DailyCount
import com.jinbo.smartsleep.data.database.SessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Statistics screen
 */
class StatisticsViewModel(
    private val repository: SessionRepository
) : ViewModel() {

    // Selected time period
    private val _selectedPeriod = MutableStateFlow(TimePeriod.SEVEN_DAYS)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    // UI State
    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Change time period and reload data
     */
    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
        loadData()
    }

    /**
     * Load data for selected period
     */
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading

            try {
                val period = _selectedPeriod.value

                // Get aggregate stats
                val stats = repository.getStatsForPeriod(period)

                // Get daily snore counts
                val dailyCounts = repository.getDailySnoreCounts(period)

                // Get all sessions for the period
                val sessions = repository.getSessionsForPeriod(period)

                _uiState.value = StatisticsUiState.Success(
                    aggregateStats = stats,
                    dailyCounts = dailyCounts,
                    sessions = sessions
                )
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Refresh data
     */
    fun refresh() {
        loadData()
    }

    companion object {
        fun provideFactory(repository: SessionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                        return StatisticsViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
                }
            }
    }
}

/**
 * UI State for Statistics screen
 */
sealed class StatisticsUiState {
    object Loading : StatisticsUiState()

    data class Success(
        val aggregateStats: AggregateStats?,
        val dailyCounts: List<DailyCount>,
        val sessions: kotlinx.coroutines.flow.Flow<List<SessionEntity>>
    ) : StatisticsUiState()

    data class Error(val message: String) : StatisticsUiState()
}
