package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.WebSocketEvent
import com.muhwezi.networkbridge.data.remote.WebSocketService
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketRepository @Inject constructor(
    private val webSocketService: WebSocketService
) {
    val events: SharedFlow<WebSocketEvent> = webSocketService.events

    fun connect() {
        webSocketService.connect()
    }

    fun disconnect() {
        webSocketService.disconnect()
    }
}
