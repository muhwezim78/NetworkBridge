package com.muhwezi.networkbridge.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.UpdateProfileRequest
import com.muhwezi.networkbridge.data.model.UserResponse
import com.muhwezi.networkbridge.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themeManager: com.muhwezi.networkbridge.data.local.ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeManager.themeMode.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
    }

    fun setThemeMode(mode: com.muhwezi.networkbridge.data.local.ThemeMode) {
        viewModelScope.launch {
            themeManager.setThemeMode(mode)
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.getCurrentUser()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, phoneNumber: String, address: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null, successMessage = null)
            val request = UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                address = address
            )
            val result = authRepository.updateProfile(request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    successMessage = "Profile updated successfully"
                )
                loadProfile() // Refresh
            } else {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to update profile"
                )
            }
        }
    }

    fun changePassword(old: String, new: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null, successMessage = null)
            val result = authRepository.changePassword(old, new)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    successMessage = "Password changed successfully"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to change password"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

data class ProfileUiState(
    val user: UserResponse? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val themeMode: com.muhwezi.networkbridge.data.local.ThemeMode = com.muhwezi.networkbridge.data.local.ThemeMode.SYSTEM
)
