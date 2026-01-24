package com.muhwezi.networkbridge.ui.mikrotik.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.PPPoEUserRequest
import com.muhwezi.networkbridge.data.repository.MikrotikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PPPoEUsersViewModel @Inject constructor(
    private val mikrotikRepository: MikrotikRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routerId: String = checkNotNull(savedStateHandle["routerId"])

    private val _uiState = MutableStateFlow(PPPoEUsersUiState())
    val uiState: StateFlow<PPPoEUsersUiState> = _uiState.asStateFlow()

    init {
        // No list endpoint for PPPoE users in current API, but we can prepare for it
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onProfileChange(profile: String) {
        _uiState.value = _uiState.value.copy(profile = profile)
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            username = "",
            password = "",
            profile = ""
        )
    }

    fun createPPPoEUser() {
        if (_uiState.value.username.isBlank() || _uiState.value.password.isBlank() || _uiState.value.profile.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "All fields are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val request = PPPoEUserRequest(
                routerId = routerId,
                username = _uiState.value.username,
                password = _uiState.value.password,
                profile = _uiState.value.profile
            )

            val result = mikrotikRepository.createPPPoEUser(request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showAddDialog = false,
                    successMessage = "PPPoE user created successfully",
                    username = "",
                    password = "",
                    profile = ""
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to create user"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

data class PPPoEUsersUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showAddDialog: Boolean = false,
    val username: String = "",
    val password: String = "",
    val profile: String = ""
)
