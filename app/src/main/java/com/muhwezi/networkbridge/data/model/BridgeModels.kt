package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class BridgeCommandRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("command") val command: String,
    @SerializedName("parameters") val parameters: Map<String, String> = emptyMap()
)

data class BridgeCommandResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class DeviceStatus(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("name") val name: String,
    @SerializedName("is_online") val isOnline: Boolean,
    @SerializedName("ip_address") val ipAddress: String
)
