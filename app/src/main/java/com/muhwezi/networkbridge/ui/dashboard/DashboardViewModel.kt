package com.muhwezi.networkbridge.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.Router
import com.muhwezi.networkbridge.data.repository.MikrotikRepository
import com.muhwezi.networkbridge.data.repository.RouterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val routerRepository: RouterRepository,
    private val mikrotikRepository: MikrotikRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = routerRepository.getRouters()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    devices = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load devices"
                )
            }
        }
    }

    fun sendCommand(deviceId: String, command: String) {
        viewModelScope.launch {
            val result = mikrotikRepository.executeCommand(deviceId, command)
            if (result.isSuccess) {
                // Optionally refresh or show success message
                loadDevices()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Command failed"
                )
            }
        }
    }
}

data class DashboardUiState(
    val devices: List<Router> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
