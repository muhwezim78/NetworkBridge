package com.muhwezi.networkbridge.ui.mikrotik.plans

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.CreateHotspotPlanRequest
import com.muhwezi.networkbridge.data.model.HotspotPlan
import com.muhwezi.networkbridge.data.repository.MikrotikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HotspotPlansViewModel @Inject constructor(
    private val mikrotikRepository: MikrotikRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routerId: String = checkNotNull(savedStateHandle["routerId"])
    
    private val _uiState = MutableStateFlow(HotspotPlansUiState())
    val uiState: StateFlow<HotspotPlansUiState> = _uiState.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = mikrotikRepository.getHotspotPlans(routerId)
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
            val result = mikrotikRepository.syncHotspotPlans(routerId)
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
            uptimeLimit = "",
            dataLimit = "",
            sharedUsers = ""
        )
    }

    fun onPlanNameChange(name: String) {
        _uiState.value = _uiState.value.copy(planName = name)
    }

    fun onPlanPriceChange(price: String) {
        _uiState.value = _uiState.value.copy(planPrice = price)
    }

    fun onUptimeLimitChange(uptime: String) {
        _uiState.value = _uiState.value.copy(uptimeLimit = uptime)
    }

    fun onDataLimitChange(data: String) {
        _uiState.value = _uiState.value.copy(dataLimit = data)
    }

    fun onSharedUsersChange(users: String) {
        _uiState.value = _uiState.value.copy(sharedUsers = users)
    }

    fun createPlan() {
        if (_uiState.value.planName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Plan name is required")
            return
        }

        val price = _uiState.value.planPrice.toDoubleOrNull()
        if (price == null || price < 0) {
            _uiState.value = _uiState.value.copy(error = "Valid price is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = CreateHotspotPlanRequest(
                routerId = routerId,
                name = _uiState.value.planName,
                price = price,
                uptimeLimit = _uiState.value.uptimeLimit.toIntOrNull(),
                dataLimit = _uiState.value.dataLimit.toLongOrNull(),
                sharedUsers = _uiState.value.sharedUsers.toIntOrNull()
            )

            val result = mikrotikRepository.createHotspotPlan(request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showCreateDialog = false,
                    successMessage = "Plan created successfully",
                    planName = "",
                    planPrice = "",
                    uptimeLimit = "",
                    dataLimit = "",
                    sharedUsers = ""
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

data class HotspotPlansUiState(
    val plans: List<HotspotPlan> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val planName: String = "",
    val planPrice: String = "",
    val uptimeLimit: String = "",
    val dataLimit: String = "",
    val sharedUsers: String = ""
)
