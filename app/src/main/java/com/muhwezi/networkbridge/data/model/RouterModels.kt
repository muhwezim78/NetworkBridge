package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class CreateRouterRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("real_ip") val realIp: String? = null,
    @SerializedName("serial_number") val serialNumber: String? = null,
    @SerializedName("ddns_name") val ddnsName: String? = null
)

data class Router(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("host_ip") val hostIp: String?,
    @SerializedName("real_ip") val realIp: String?,
    @SerializedName("username") val username: String,
    @SerializedName("status") val status: String,
    @SerializedName("serial_number") val serialNumber: String,
    @SerializedName("ddns_name") val ddnsName: String?,
    @SerializedName("wg_public_key") val wgPublicKey: String?,
    @SerializedName("active_users") val activeUsers: Int?,
    @SerializedName("last_seen") val lastSeen: String?,
    @SerializedName("wireguard_config") val wireguardConfig: String? = null, // For details view
    @SerializedName("setup_script") val setupScript: String? = null // For create response
)
