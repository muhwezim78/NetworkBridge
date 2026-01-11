package com.muhwezi.networkbridge.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.local.TokenManager
import com.muhwezi.networkbridge.data.model.SubscriptionStatus
import com.muhwezi.networkbridge.data.repository.AuthRepository
import com.muhwezi.networkbridge.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadSubscriptionStatus()
        checkUserRole()
    }

    private fun checkUserRole() {
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isAdmin = result.getOrNull()?.role == "admin"
                )
            }
        }
    }

    fun loadSubscriptionStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = subscriptionRepository.getStatus()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    subscriptionStatus = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load subscription"
                )
            }
        }
    }

    fun onRedeemCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(redeemCode = code)
    }

    fun redeemToken() {
        if (_uiState.value.redeemCode.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a code")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = tokenManager.userId.first()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not found"
                )
                return@launch
            }

            val result = subscriptionRepository.redeemToken(_uiState.value.redeemCode, userId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    redeemCode = "",
                    successMessage = "Token redeemed successfully!"
                )
                loadSubscriptionStatus()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to redeem token"
                )
            }
        }
    }

    fun onPackageTypeChange(packageType: String) {
        _uiState.value = _uiState.value.copy(packageType = packageType)
    }

    fun onDurationChange(duration: String) {
        _uiState.value = _uiState.value.copy(duration = duration)
    }

    fun generateToken() {
        if (_uiState.value.packageType.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter package type")
            return
        }

        val durationDays = _uiState.value.duration.toIntOrNull()
        if (durationDays == null || durationDays <= 0) {
            _uiState.value = _uiState.value.copy(error = "Please enter valid duration in days")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = subscriptionRepository.generateToken(_uiState.value.packageType, durationDays)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generatedToken = result.getOrNull()?.code,
                    packageType = "",
                    duration = "",
                    successMessage = "Token generated successfully!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to generate token"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

data class SubscriptionUiState(
    val subscriptionStatus: SubscriptionStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val redeemCode: String = "",
    val isAdmin: Boolean = false,
    val packageType: String = "",
    val duration: String = "",
    val generatedToken: String? = null
)
