package com.muhwezi.networkbridge.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.NotificationResponse
import com.muhwezi.networkbridge.data.model.WebSocketEvent
import com.muhwezi.networkbridge.data.remote.WebSocketService
import com.muhwezi.networkbridge.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val webSocketService: WebSocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchNotifications()
        observeWebSocketEvents()
    }

    private fun observeWebSocketEvents() {
        viewModelScope.launch {
            webSocketService.events
                .filterIsInstance<WebSocketEvent.NewNotification>()
                .collect { event ->
                    // Prepend the new notification from WebSocket without a full network call
                    val newItem = NotificationResponse(
                        id = event.id,
                        title = event.title,
                        message = event.message,
                        level = event.level,
                        isRead = false,
                        createdAt = event.createdAt
                    )
                    val currentState = _uiState.value
                    val updatedList = if (currentState is NotificationUiState.Success) {
                        listOf(newItem) + currentState.notifications
                    } else {
                        listOf(newItem)
                    }
                    _uiState.value = NotificationUiState.Success(updatedList)
                    _unreadCount.value = updatedList.count { !it.isRead }
                }
        }
    }

    fun fetchNotifications() {
        _uiState.value = NotificationUiState.Loading
        viewModelScope.launch {
            val result = repository.getNotifications()
            result.onSuccess { notifications ->
                _uiState.value = NotificationUiState.Success(notifications)
                _unreadCount.value = notifications.count { !it.isRead }
            }.onFailure { error ->
                _uiState.value = NotificationUiState.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.getNotifications()
            result.onSuccess { notifications ->
                _uiState.value = NotificationUiState.Success(notifications)
                _unreadCount.value = notifications.count { !it.isRead }
            }.onFailure { error ->
                // Keep existing list visible on pull-to-refresh failure, just stop spinning
                if (_uiState.value !is NotificationUiState.Success) {
                    _uiState.value = NotificationUiState.Error(error.message ?: "Refresh failed")
                }
            }
            _isRefreshing.value = false
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            val result = repository.markNotificationRead(id)
            if (result.isSuccess) {
                val currentState = _uiState.value
                if (currentState is NotificationUiState.Success) {
                    val updatedList = currentState.notifications.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    }
                    _uiState.value = NotificationUiState.Success(updatedList)
                    _unreadCount.value = updatedList.count { !it.isRead }
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val result = repository.markAllNotificationsRead()
            if (result.isSuccess) {
                val currentState = _uiState.value
                if (currentState is NotificationUiState.Success) {
                    val updatedList = currentState.notifications.map { it.copy(isRead = true) }
                    _uiState.value = NotificationUiState.Success(updatedList)
                    _unreadCount.value = 0
                }
            }
        }
    }
}

sealed class NotificationUiState {
    object Loading : NotificationUiState()
    data class Success(val notifications: List<NotificationResponse>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}
