package com.muhwezi.networkbridge.ui.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.UserItem
import com.muhwezi.networkbridge.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.getUsers()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    users = result.getOrNull()?.users ?: emptyList(),
                    totalUsers = result.getOrNull()?.total ?: 0
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load users"
                )
            }
        }
    }

    fun showRoleDialog(user: UserItem) {
        _uiState.value = _uiState.value.copy(
            showRoleDialog = true,
            selectedUser = user,
            selectedRole = user.role
        )
    }

    fun hideRoleDialog() {
        _uiState.value = _uiState.value.copy(
            showRoleDialog = false,
            selectedUser = null,
            selectedRole = ""
        )
    }

    fun onRoleChange(role: String) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun updateUserRole() {
        val user = _uiState.value.selectedUser ?: return
        val role = _uiState.value.selectedRole

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.updateUserRole(user.id, role)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showRoleDialog = false,
                    successMessage = "Role updated successfully"
                )
                loadUsers()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to update role"
                )
            }
        }
    }

    fun toggleUserStatus(user: UserItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.updateUserStatus(user.id, !user.isActive)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "User status updated"
                )
                loadUsers()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to update status"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

data class UserManagementUiState(
    val users: List<UserItem> = emptyList(),
    val totalUsers: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showRoleDialog: Boolean = false,
    val selectedUser: UserItem? = null,
    val selectedRole: String = ""
)
