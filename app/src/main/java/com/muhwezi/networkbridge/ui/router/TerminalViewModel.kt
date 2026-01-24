package com.muhwezi.networkbridge.ui.router

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.repository.MikrotikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val mikrotikRepository: MikrotikRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routerId: String = checkNotNull(savedStateHandle["routerId"])

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    fun onCommandChange(command: String) {
        _uiState.value = _uiState.value.copy(command = command)
    }

    fun executeCommand() {
        val cmd = _uiState.value.command
        if (cmd.isBlank()) return

        viewModelScope.launch {
            val currentOutput = _uiState.value.output
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                output = "$currentOutput\n> $cmd"
            )

            val result = mikrotikRepository.executeCommand(routerId, cmd)
            if (result.isSuccess) {
                val outputText = result.getOrNull()?.output?.toString() ?: "No output"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    output = "${_uiState.value.output}\n$outputText",
                    command = ""
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    output = "${_uiState.value.output}\nError: ${result.exceptionOrNull()?.message}",
                    command = ""
                )
            }
        }
    }

    fun clearOutput() {
        _uiState.value = _uiState.value.copy(output = "")
    }
}

data class TerminalUiState(
    val command: String = "",
    val output: String = "MikroTik Terminal Ready\nType a command (e.g. /system/resource/print)",
    val isLoading: Boolean = false
)
