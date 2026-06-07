package com.muhwezi.networkbridge.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.UserSmsLogResponse
import com.muhwezi.networkbridge.data.model.UserSmsStatsResponse
import com.muhwezi.networkbridge.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsAnalyticsUiState(
    val stats: UserSmsStatsResponse? = null,
    val logs: List<UserSmsLogResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SmsAnalyticsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsAnalyticsUiState())
    val uiState: StateFlow<SmsAnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val statsResult = notificationRepository.getSmsStats()
            val logsResult = notificationRepository.getSmsLogs()
            
            if (statsResult.isSuccess && logsResult.isSuccess) {
                _uiState.update { 
                    it.copy(
                        stats = statsResult.getOrNull(),
                        logs = logsResult.getOrNull() ?: emptyList(),
                        isLoading = false
                    ) 
                }
            } else {
                val errorMsg = statsResult.exceptionOrNull()?.message ?: logsResult.exceptionOrNull()?.message ?: "Failed to load SMS data"
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
