package com.muhwezi.networkbridge.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.muhwezi.networkbridge.data.local.TokenManager
import com.muhwezi.networkbridge.data.model.WebSocketEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenManager: TokenManager,
    private val gson: Gson
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var isConnecting = false
    private val _events = MutableSharedFlow<WebSocketEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<WebSocketEvent> = _events.asSharedFlow()

    fun connect() {
        if (isConnecting || webSocket != null) return
        isConnecting = true

        scope.launch {
            val token = tokenManager.token.first()
            if (token == null) {
                isConnecting = false
                return@launch
            }

            val wsUrl = com.muhwezi.networkbridge.BuildConfig.API_BASE_URL
                .replace("http", "ws")
                .substringBeforeLast("api/") + "ws?token=$token"
            
            val request = Request.Builder()
                .url(wsUrl)
                .build()

            webSocket = okHttpClient.newWebSocket(request, createListener())
        }
    }

    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            isConnecting = false
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val jsonObject = gson.fromJson(text, JsonObject::class.java)
                val type = jsonObject.get("type")?.asString
                
                val event = when (type) {
                    "router_status" -> gson.fromJson(text, WebSocketEvent.RouterStatus::class.java)
                    "voucher_activated" -> gson.fromJson(text, WebSocketEvent.VoucherActivated::class.java)
                    "income_recorded" -> gson.fromJson(text, WebSocketEvent.IncomeRecorded::class.java)
                    "dashboard_stats" -> gson.fromJson(text, WebSocketEvent.DashboardStats::class.java)
                    else -> null
                }

                if (event != null) {
                    _events.tryEmit(event)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            this@WebSocketService.webSocket = null
            reconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            this@WebSocketService.webSocket = null
            isConnecting = false
            reconnect()
        }
    }

    private fun reconnect() {
        scope.launch {
            delay(5000)
            connect()
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
    }
}
