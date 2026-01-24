package com.muhwezi.networkbridge.ui.mikrotik.plans

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.CreatePPPoEPlanRequest
import com.muhwezi.networkbridge.data.model.PPPoEPlan
import com.muhwezi.networkbridge.data.repository.MikrotikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PPPoEPlansViewModel @Inject constructor(
    private val mikrotikRepository: MikrotikRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routerId: String = checkNotNull(savedStateHandle["routerId"])
    
    private val _uiState = MutableStateFlow(PPPoEPlansUiState())
    val uiState: StateFlow<PPPoEPlansUiState> = _uiState.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = mikrotikRepository.getPPPoEPlans(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    plans = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load plans"
                )
            }
        }
    }

    fun syncPlans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = mikrotikRepository.syncPPPoEPlans(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Plans synced successfully"
                )
                loadPlans()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to sync plans"
                )
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false,
            planName = "",
            planPrice = "",
            localAddress = "",
            remoteAddress = "",
            rateLimit = ""
        )
    }

    fun onPlanNameChange(name: String) {
        _uiState.value = _uiState.value.copy(planName = name)
    }

    fun onPlanPriceChange(price: String) {
        _uiState.value = _uiState.value.copy(planPrice = price)
    }

    fun onLocalAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(localAddress = address)
    }

    fun onRemoteAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(remoteAddress = address)
    }

    fun onRateLimitChange(limit: String) {
        _uiState.value = _uiState.value.copy(rateLimit = limit)
    }

    fun createPlan() {
        if (_uiState.value.planName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Plan name is required")
            return
        }

        val price = _uiState.value.planPrice.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = CreatePPPoEPlanRequest(
                routerId = routerId,
                name = _uiState.value.planName,
                price = price,
                localAddress = _uiState.value.localAddress,
                remoteAddress = _uiState.value.remoteAddress,
                rateLimit = _uiState.value.rateLimit
            )

            val result = mikrotikRepository.createPPPoEPlan(request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showCreateDialog = false,
                    successMessage = "Plan created successfully",
                    planName = "",
                    planPrice = "",
                    localAddress = "",
                    remoteAddress = "",
                    rateLimit = ""
                )
                loadPlans()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to create plan"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

data class PPPoEPlansUiState(
    val plans: List<PPPoEPlan> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val planName: String = "",
    val planPrice: String = "",
    val localAddress: String = "",
    val remoteAddress: String = "",
    val rateLimit: String = ""
)
