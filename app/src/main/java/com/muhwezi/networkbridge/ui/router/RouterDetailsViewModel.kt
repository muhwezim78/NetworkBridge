package com.muhwezi.networkbridge.ui.router

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.*
import com.muhwezi.networkbridge.data.repository.RouterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouterDetailsViewModel @Inject constructor(
    private val routerRepository: RouterRepository,
    private val mikrotikRepository: com.muhwezi.networkbridge.data.repository.MikrotikRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routerId: String = checkNotNull(savedStateHandle["routerId"])
    private val _uiState = MutableStateFlow(RouterDetailsUiState())
    val uiState: StateFlow<RouterDetailsUiState> = _uiState.asStateFlow()

    init {
        loadRouterDetails()
    }

    fun loadRouterDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = routerRepository.getRouter(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    router = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load router details"
                )
            }
        }
    }

    fun deleteRouter(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = routerRepository.deleteRouter(routerId)
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to delete router"
                )
            }
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLogs = true)
            val result = routerRepository.getRouterLogs(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoadingLogs = false, logs = result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = _uiState.value.copy(isLoadingLogs = false, error = "Failed to load logs")
            }
        }
    }

    fun loadBackups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingBackups = true)
            val result = routerRepository.getRouterBackups(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoadingBackups = false, backups = result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = _uiState.value.copy(isLoadingBackups = false, error = "Failed to load backups")
            }
        }
    }

    fun loadIPPools() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPools = true)
            val result = routerRepository.getIPPools(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoadingPools = false, pools = result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = _uiState.value.copy(isLoadingPools = false, error = "Failed to load IP pools")
            }
        }
    }

    fun loadTrafficStats() {
        viewModelScope.launch {
            val result = routerRepository.getTrafficStats(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(trafficStats = result.getOrNull() ?: emptyList())
            }
        }
    }

    fun setupTrafficFlow() {
        viewModelScope.launch {
            val result = routerRepository.setupTrafficFlow(routerId)
            if (result.isSuccess) {
                // Show success message or reload
            }
        }
    }

    fun setupRemoteLogging() {
        viewModelScope.launch {
            val result = routerRepository.setupRemoteLogging(routerId)
            if (result.isSuccess) {
                // Show success message or reload
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingBackups = true)
            val name = "manual_backup_${System.currentTimeMillis()}"
            val command = "/system backup save name=$name"
            
            val result = mikrotikRepository.executeCommand(routerId, command)
            if (result.isSuccess) {
                // Wait a bit for filesystem to update
                kotlinx.coroutines.delay(2000) 
                loadBackups()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingBackups = false, 
                    error = result.exceptionOrNull()?.message ?: "Failed to create backup"
                )
            }
        }
    }
}

data class RouterDetailsUiState(
    val router: Router? = null,
    val logs: List<RouterLog> = emptyList(),
    val backups: List<RouterBackup> = emptyList(),
    val pools: List<IPPool> = emptyList(),
    val trafficStats: List<TrafficStat> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingLogs: Boolean = false,
    val isLoadingBackups: Boolean = false,
    val isLoadingPools: Boolean = false,
    val error: String? = null
)
