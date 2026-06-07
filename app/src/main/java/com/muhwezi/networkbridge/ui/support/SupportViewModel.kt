package com.muhwezi.networkbridge.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.ChatMessageResponse
import com.muhwezi.networkbridge.data.repository.SupportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupportUiState(
    val messages: List<ChatMessageResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val messageInput: String = "",
    val error: String? = null
)

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val supportRepository: SupportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = supportRepository.getMessages()
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        messages = result.getOrNull() ?: emptyList(),
                        isLoading = false
                    ) 
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = result.exceptionOrNull()?.message ?: "Failed to load messages"
                    ) 
                }
            }
        }
    }

    fun onMessageInputChange(input: String) {
        _uiState.update { it.copy(messageInput = input) }
    }

    fun sendMessage() {
        val content = _uiState.value.messageInput.trim()
        if (content.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            val result = supportRepository.sendMessage(content)
            if (result.isSuccess) {
                _uiState.update { it.copy(isSending = false, messageInput = "") }
                loadMessages() // Reload messages after sending
            } else {
                _uiState.update { 
                    it.copy(
                        isSending = false, 
                        error = result.exceptionOrNull()?.message ?: "Failed to send message"
                    ) 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
