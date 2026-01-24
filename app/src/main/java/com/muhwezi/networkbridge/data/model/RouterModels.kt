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
    @SerializedName("setup_script") val setupScript: String? = null, // For create response
    @SerializedName("cpu_load") val cpuLoad: String? = null,
    @SerializedName("memory_usage") val memoryUsage: String? = null,
    @SerializedName("uptime") val uptime: String? = null,
    @SerializedName("os_version") val osVersion: String? = null,
    @SerializedName("board_name") val boardName: String? = null
)

data class RouterLog(
    @SerializedName("id") val id: String,
    @SerializedName("router_id") val routerId: String? = null,
    @SerializedName("facility") val facility: String? = null,
    @SerializedName("severity") val severity: String? = null,
    @SerializedName("topic") val topic: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class RouterBackup(
    @SerializedName("id") val id: String,
    @SerializedName("router_id") val routerId: String? = null,
    @SerializedName("filename") val filename: String? = null,
    @SerializedName("backup_type") val backupType: String? = null,
    @SerializedName("file_size") val fileSize: Long? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class IPPool(
    @SerializedName("id") val id: String,
    @SerializedName("router_id") val routerId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("ranges") val ranges: String? = null,
    @SerializedName("used_count") val usedCount: Int = 0,
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class TrafficStat(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("bytes_in") val bytesIn: Long,
    @SerializedName("bytes_out") val bytesOut: Long
)

data class GeneratePdfRequest(
    @SerializedName("router_id") val routerId: String,
    @SerializedName("vouchers") val vouchers: List<VoucherInfo>,
    @SerializedName("include_qr") val includeQr: Boolean = true
)

data class VoucherInfo(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)
