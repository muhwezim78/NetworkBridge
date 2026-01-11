package com.muhwezi.networkbridge.ui.mikrotik.vouchers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.GenerateVouchersRequest
import com.muhwezi.networkbridge.data.model.VoucherResponse
import com.muhwezi.networkbridge.data.repository.MikrotikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoucherViewModel @Inject constructor(
    private val mikrotikRepository: MikrotikRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routerId: String = checkNotNull(savedStateHandle["routerId"])
    
    private val _uiState = MutableStateFlow(VoucherUiState())
    val uiState: StateFlow<VoucherUiState> = _uiState.asStateFlow()

    init {
        loadVouchers()
    }

    fun loadVouchers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = mikrotikRepository.getVouchers(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    vouchers = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load vouchers"
                )
            }
        }
    }

    fun showGenerateDialog() {
        _uiState.value = _uiState.value.copy(showGenerateDialog = true)
    }

    fun hideGenerateDialog() {
        _uiState.value = _uiState.value.copy(
            showGenerateDialog = false,
            planId = "",
            count = "",
            passwordMode = "random",
            length = "8"
        )
    }

    fun onPlanIdChange(planId: String) {
        _uiState.value = _uiState.value.copy(planId = planId)
    }

    fun onCountChange(count: String) {
        _uiState.value = _uiState.value.copy(count = count)
    }

    fun onPasswordModeChange(mode: String) {
        _uiState.value = _uiState.value.copy(passwordMode = mode)
    }

    fun onLengthChange(length: String) {
        _uiState.value = _uiState.value.copy(length = length)
    }

    fun generateVouchers() {
        if (_uiState.value.planId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Plan ID is required")
            return
        }

        val count = _uiState.value.count.toIntOrNull()
        if (count == null || count <= 0) {
            _uiState.value = _uiState.value.copy(error = "Valid count is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = GenerateVouchersRequest(
                routerId = routerId,
                planId = _uiState.value.planId,
                count = count,
                passwordMode = _uiState.value.passwordMode,
                length = _uiState.value.length.toIntOrNull()
            )

            val result = mikrotikRepository.generateVouchers(request)
            if (result.isSuccess) {
                val response = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showGenerateDialog = false,
                    successMessage = "Generated ${response?.vouchersCreated ?: count} vouchers",
                    planId = "",
                    count = "",
                    passwordMode = "random",
                    length = "8"
                )
                loadVouchers()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to generate vouchers"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

data class VoucherUiState(
    val vouchers: List<VoucherResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showGenerateDialog: Boolean = false,
    val planId: String = "",
    val count: String = "",
    val passwordMode: String = "random",
    val length: String = "8"
)
