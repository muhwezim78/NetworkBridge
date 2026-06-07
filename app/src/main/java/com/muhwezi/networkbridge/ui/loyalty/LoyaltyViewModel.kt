package com.muhwezi.networkbridge.ui.loyalty

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.LoyaltySettingsResponse
import com.muhwezi.networkbridge.data.repository.LoyaltyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoyaltyUiState(
    val routerId: String = "",
    val settings: LoyaltySettingsResponse? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isProgramEnabled: Boolean = false,
    val rewardType: String = "data_bonus",
    val rewardValue: String = "100MB",
    val minPurchaseRequired: String = "1000",
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class LoyaltyViewModel @Inject constructor(
    private val repository: LoyaltyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoyaltyUiState())
    val uiState: StateFlow<LoyaltyUiState> = _uiState.asStateFlow()

    fun loadData(routerId: String) {
        _uiState.update { it.copy(routerId = routerId, isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.getLoyaltySettings(routerId)
            if (result.isSuccess) {
                val settings = result.getOrNull()
                _uiState.update {
                    it.copy(
                        settings = settings,
                        isLoading = false,
                        isProgramEnabled = settings?.programEnabled ?: false,
                        rewardType = settings?.rewardType ?: "data_bonus",
                        rewardValue = settings?.rewardValue ?: "100MB",
                        minPurchaseRequired = settings?.minPurchaseRequired?.toString() ?: "1000"
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to load loyalty settings"
                    )
                }
            }
        }
    }

    fun toggleProgramEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isProgramEnabled = enabled) }
    }

    fun onRewardTypeChange(type: String) {
        _uiState.update { it.copy(rewardType = type) }
    }

    fun onRewardValueChange(value: String) {
        _uiState.update { it.copy(rewardValue = value) }
    }

    fun onMinPurchaseChange(value: String) {
        _uiState.update { it.copy(minPurchaseRequired = value) }
    }

    fun saveSettings() {
        val currentState = _uiState.value
        val routerId = currentState.routerId
        if (routerId.isEmpty()) return

        _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }

        viewModelScope.launch {
            val minPurchase = currentState.minPurchaseRequired.toDoubleOrNull()
            
            val result = repository.updateLoyaltySettings(
                routerId = routerId,
                enabled = currentState.isProgramEnabled,
                rewardType = currentState.rewardType,
                rewardValue = currentState.rewardValue,
                minPurchase = minPurchase
            )

            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        successMessage = "Loyalty settings saved successfully"
                    ) 
                }
                loadData(routerId) // Refresh
            } else {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to save settings"
                    ) 
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
