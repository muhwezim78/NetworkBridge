package com.muhwezi.networkbridge.ui.router

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.Router
import com.muhwezi.networkbridge.data.repository.RouterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouterDetailsViewModel @Inject constructor(
    private val routerRepository: RouterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routerId: String = checkNotNull(savedStateHandle["routerId"])
    private val _uiState = MutableStateFlow(RouterDetailsUiState())
    val uiState: StateFlow<RouterDetailsUiState> = _uiState.asStateFlow()

    init {
        loadRouterDetails()
    }

    fun loadRouterDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = routerRepository.getRouter(routerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    router = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load router details"
                )
            }
        }
    }

    fun deleteRouter(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = routerRepository.deleteRouter(routerId)
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to delete router"
                )
            }
        }
    }
}

data class RouterDetailsUiState(
    val router: Router? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
