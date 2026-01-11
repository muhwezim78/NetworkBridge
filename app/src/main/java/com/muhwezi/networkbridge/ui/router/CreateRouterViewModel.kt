package com.muhwezi.networkbridge.ui.router

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.local.TokenManager
import com.muhwezi.networkbridge.data.model.CreateRouterRequest
import com.muhwezi.networkbridge.data.repository.RouterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRouterViewModel @Inject constructor(
    private val routerRepository: RouterRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRouterUiState())
    val uiState: StateFlow<CreateRouterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onIpChange(ip: String) {
        _uiState.value = _uiState.value.copy(ipAddress = ip)
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onPortChange(port: String) {
        // Simple validation or just store string and convert later
        _uiState.value = _uiState.value.copy(apiPort = port)
    }

    fun createRouter() {
        // Prevent multiple simultaneous submissions
        if (_uiState.value.isLoading) return
        
        // Validate required fields
        if (_uiState.value.name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Router name is required")
            return
        }
        
        // IP address is now optional (real_ip), but if user provides it, we validate?
        // Wait, spec says real_ip is optional. But CreateRouterRequest has it as optional.
        // The UI has IP Address field. I'll assume it maps to real_ip.
        // If it's optional in spec, I should allow blank?
        // But previously it was required. I'll keep it required if the UI implies it, or make it optional.
        // The UI state has ipAddress. I'll map it to realIp.
        
        if (_uiState.value.username.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Username is required")
            return
        }
        
        if (_uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Password is required")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val userId = tokenManager.userId.first()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not found. Please login again.")
                return@launch
            }

            val request = CreateRouterRequest(
                userId = userId,
                name = _uiState.value.name,
                username = _uiState.value.username,
                password = _uiState.value.password,
                realIp = _uiState.value.ipAddress.ifBlank { null },
                // serialNumber and ddnsName are not in UI state yet, so null
            )

            val result = routerRepository.createRouter(request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }
}

data class CreateRouterUiState(
    val name: String = "",
    val ipAddress: String = "",
    val username: String = "",
    val password: String = "",
    val apiPort: String = "", // Not used in new request but kept for UI state compatibility if needed
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
