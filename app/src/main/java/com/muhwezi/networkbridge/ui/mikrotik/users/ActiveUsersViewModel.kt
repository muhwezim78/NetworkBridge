package com.muhwezi.networkbridge.ui.mikrotik.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.ActiveHotspotUser
import com.muhwezi.networkbridge.data.repository.MikrotikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveUsersViewModel @Inject constructor(
    private val mikrotikRepository: MikrotikRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routerId: String = checkNotNull(savedStateHandle["routerId"])
    
    private val _uiState = MutableStateFlow(ActiveUsersUiState())
    val uiState: StateFlow<ActiveUsersUiState> = _uiState.asStateFlow()

    init {
        loadActiveUsers()
    }

    fun loadActiveUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = mikrotikRepository.getActiveHotspotUsers(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeUsers = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load active users"
                )
            }
        }
    }
}

data class ActiveUsersUiState(
    val activeUsers: List<ActiveHotspotUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
