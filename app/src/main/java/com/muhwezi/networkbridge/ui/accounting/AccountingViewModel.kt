package com.muhwezi.networkbridge.ui.accounting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.DashboardStats
import com.muhwezi.networkbridge.data.model.IncomeReport
import com.muhwezi.networkbridge.data.repository.AccountingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountingViewModel @Inject constructor(
    private val accountingRepository: AccountingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountingUiState())
    val uiState: StateFlow<AccountingUiState> = _uiState.asStateFlow()

    init {
        loadDashboardStats()
        loadRevenueReport()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStats = true, error = null)
            val result = accountingRepository.getDashboardStats()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoadingStats = false,
                    dashboardStats = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingStats = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load stats"
                )
            }
        }
    }

    fun loadRevenueReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRevenue = true, error = null)
            val result = accountingRepository.getRevenueReport(
                limit = 30 // Last 30 entries
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoadingRevenue = false,
                    revenueReports = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingRevenue = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load revenue"
                )
            }
        }
    }

    fun refresh() {
        loadDashboardStats()
        loadRevenueReport()
    }
}

data class AccountingUiState(
    val dashboardStats: DashboardStats? = null,
    val revenueReports: List<IncomeReport> = emptyList(),
    val isLoadingStats: Boolean = false,
    val isLoadingRevenue: Boolean = false,
    val error: String? = null
)
