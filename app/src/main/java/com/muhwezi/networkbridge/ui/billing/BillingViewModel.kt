package com.muhwezi.networkbridge.ui.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.Transaction
import com.muhwezi.networkbridge.data.model.WalletResponse
import com.muhwezi.networkbridge.data.model.WithdrawalHistoryItem
import com.muhwezi.networkbridge.data.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Launch parallel requests
            val walletResult = billingRepository.getWallet()
            val transactionsResult = billingRepository.getTransactions()
            val withdrawalsResult = billingRepository.getWithdrawals()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                wallet = walletResult.getOrNull(),
                transactions = transactionsResult.getOrDefault(emptyList()),
                withdrawals = withdrawalsResult.getOrDefault(emptyList()),
                error = walletResult.exceptionOrNull()?.message 
                    ?: transactionsResult.exceptionOrNull()?.message 
                    ?: withdrawalsResult.exceptionOrNull()?.message
            )
        }
    }

    fun onWithdrawAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(withdrawAmount = amount)
    }

    fun onWithdrawPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(withdrawPhone = phone)
    }

    fun requestWithdrawal() {
        val amount = _uiState.value.withdrawAmount.toDoubleOrNull()
        val phone = _uiState.value.withdrawPhone

        if (amount == null || amount <= 0) {
            _uiState.value = _uiState.value.copy(error = "Invalid amount")
            return
        }
        if (phone.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Phone number required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWithdrawing = true, error = null)
            val result = billingRepository.requestWithdrawal(amount, phone)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isWithdrawing = false,
                    successMessage = "Withdrawal requested successfully!",
                    withdrawAmount = "",
                    withdrawPhone = "",
                    showWithdrawDialog = false
                )
                loadData() // Refresh wallet and history
            } else {
                _uiState.value = _uiState.value.copy(
                    isWithdrawing = false,
                    error = result.exceptionOrNull()?.message ?: "Withdrawal failed"
                )
            }
        }
    }

    fun showWithdrawDialog() {
        _uiState.value = _uiState.value.copy(showWithdrawDialog = true)
    }

    fun hideWithdrawDialog() {
        _uiState.value = _uiState.value.copy(
            showWithdrawDialog = false, 
            withdrawAmount = "", 
            withdrawPhone = ""
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

data class BillingUiState(
    val wallet: WalletResponse? = null,
    val transactions: List<Transaction> = emptyList(),
    val withdrawals: List<WithdrawalHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isWithdrawing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showWithdrawDialog: Boolean = false,
    val withdrawAmount: String = "",
    val withdrawPhone: String = ""
)
