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
import com.muhwezi.networkbridge.data.model.WebSocketEvent
import com.muhwezi.networkbridge.data.repository.WebSocketRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val routerRepository: RouterRepository,
    private val mikrotikRepository: MikrotikRepository,
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
        observeLiveEvents()
    }

    private fun observeLiveEvents() {
        webSocketRepository.connect()
        viewModelScope.launch {
            webSocketRepository.events.collect { event ->
                handleWebSocketEvent(event)
            }
        }
    }

    private fun handleWebSocketEvent(event: WebSocketEvent) {
        when (event) {
            is WebSocketEvent.RouterStatus -> {
                val updatedDevices = _uiState.value.devices.map {
                    if (it.id == event.routerId) it.copy(status = event.status) else it
                }
                _uiState.value = _uiState.value.copy(devices = updatedDevices, isLive = true)
            }
            is WebSocketEvent.DashboardStats -> {
                _uiState.value = _uiState.value.copy(
                    totalRouters = event.totalRouters,
                    routersOnline = event.routersOnline,
                    activeVouchers = event.activeVouchers,
                    totalRevenue = event.totalRevenue,
                    todayIncome = event.todayIncome,
                    isLive = true
                )
            }
            else -> { /* Other events could be handled if needed (popups/notifications) */ }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketRepository.disconnect()
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
    val error: String? = null,
    val isLive: Boolean = false,
    val totalRouters: Long = 0,
    val routersOnline: Long = 0,
    val activeVouchers: Long = 0,
    val totalRevenue: Double = 0.0,
    val todayIncome: Double = 0.0
)
