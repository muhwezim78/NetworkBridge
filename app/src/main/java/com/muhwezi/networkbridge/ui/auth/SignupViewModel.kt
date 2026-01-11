package com.muhwezi.networkbridge.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.SignupRequest
import com.muhwezi.networkbridge.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun onPhoneNumberChange(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        // At least 8 characters, contains uppercase, lowercase, and number
        if (password.length < 8) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isLowerCase() }) return false
        if (!password.any { it.isDigit() }) return false
        return true
    }

    fun signup() {
        // Prevent multiple simultaneous signup attempts
        if (_uiState.value.isLoading) return
        
        // Validate email
        if (_uiState.value.email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email is required")
            return
        }
        
        if (!isValidEmail(_uiState.value.email)) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
            return
        }
        
        // Validate password
        if (_uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Password is required")
            return
        }
        
        if (!isValidPassword(_uiState.value.password)) {
            _uiState.value = _uiState.value.copy(
                error = "Password must be at least 8 characters with uppercase, lowercase, and number"
            )
            return
        }
        
        // Validate password confirmation
        if (_uiState.value.confirmPassword != _uiState.value.password) {
            _uiState.value = _uiState.value.copy(error = "Passwords do not match")
            return
        }
        
        // Validate phone number if provided
        if (_uiState.value.phoneNumber.isNotBlank() && _uiState.value.phoneNumber.length < 10) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid phone number")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val request = SignupRequest(
                email = _uiState.value.email,
                password = _uiState.value.password,
                phoneNumber = _uiState.value.phoneNumber.ifBlank { null }
            )
            val result = authRepository.signup(request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSignedUp = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Signup failed"
                )
            }
        }
    }
}

data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isSignedUp: Boolean = false,
    val error: String? = null
)
