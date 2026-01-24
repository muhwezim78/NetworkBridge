package com.muhwezi.networkbridge.ui.admin.firewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.AddGlobalFirewallRequest
import com.muhwezi.networkbridge.data.model.GlobalFirewallAddress
import com.muhwezi.networkbridge.data.repository.FirewallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalFirewallViewModel @Inject constructor(
    private val firewallRepository: FirewallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlobalFirewallUiState())
    val uiState: StateFlow<GlobalFirewallUiState> = _uiState.asStateFlow()

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = firewallRepository.getGlobalFirewallAddresses()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    addresses = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load addresses"
                )
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            listName = "",
            address = "",
            comment = ""
        )
    }

    fun onListNameChange(name: String) {
        _uiState.value = _uiState.value.copy(listName = name)
    }

    fun onAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun onCommentChange(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment)
    }

    fun addAddress() {
        if (_uiState.value.listName.isBlank() || _uiState.value.address.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "List name and address are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = AddGlobalFirewallRequest(
                listName = _uiState.value.listName,
                address = _uiState.value.address,
                comment = _uiState.value.comment
            )

            val result = firewallRepository.addGlobalFirewallAddress(request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showAddDialog = false,
                    successMessage = "Address added successfully",
                    listName = "",
                    address = "",
                    comment = ""
                )
                loadAddresses()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to add address"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

data class GlobalFirewallUiState(
    val addresses: List<GlobalFirewallAddress> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showAddDialog: Boolean = false,
    val listName: String = "",
    val address: String = "",
    val comment: String = ""
)
