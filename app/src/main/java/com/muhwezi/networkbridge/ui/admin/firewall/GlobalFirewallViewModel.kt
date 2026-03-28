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
                    error = result.exceptionOrNull()?.message ?: "Failed to load rules"
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
            action = "drop",
            chain = "forward",
            srcAddress = "",
            dstAddress = "",
            protocol = "",
            dstPort = "",
            comment = ""
        )
    }

    fun onActionChange(action: String) {
        _uiState.value = _uiState.value.copy(action = action)
    }

    fun onChainChange(chain: String) {
        _uiState.value = _uiState.value.copy(chain = chain)
    }

    fun onSrcAddressChange(srcAddress: String) {
        _uiState.value = _uiState.value.copy(srcAddress = srcAddress)
    }

    fun onDstAddressChange(dstAddress: String) {
        _uiState.value = _uiState.value.copy(dstAddress = dstAddress)
    }

    fun onProtocolChange(protocol: String) {
        _uiState.value = _uiState.value.copy(protocol = protocol)
    }

    fun onDstPortChange(dstPort: String) {
        _uiState.value = _uiState.value.copy(dstPort = dstPort)
    }

    fun onCommentChange(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment)
    }

    fun addRule() {
        if (_uiState.value.chain.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Chain is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val request = AddGlobalFirewallRequest(
                action = _uiState.value.action,
                chain = _uiState.value.chain,
                srcAddress = _uiState.value.srcAddress.ifBlank { null },
                dstAddress = _uiState.value.dstAddress.ifBlank { null },
                protocol = _uiState.value.protocol.ifBlank { null },
                dstPort = _uiState.value.dstPort.ifBlank { null },
                comment = _uiState.value.comment.ifBlank { null }
            )

            val result = firewallRepository.addGlobalFirewallAddress(request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showAddDialog = false,
                    successMessage = "Rule added successfully",
                    action = "drop",
                    chain = "forward",
                    srcAddress = "",
                    dstAddress = "",
                    protocol = "",
                    dstPort = "",
                    comment = ""
                )
                loadAddresses()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to add rule"
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
    val action: String = "drop",
    val chain: String = "forward",
    val srcAddress: String = "",
    val dstAddress: String = "",
    val protocol: String = "",
    val dstPort: String = "",
    val comment: String = ""
)
