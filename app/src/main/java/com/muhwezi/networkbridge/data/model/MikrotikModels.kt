package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class PPPoEUserRequest(
    @SerializedName("router_id") val routerId: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("profile") val profile: String
)

data class CreateHotspotPlanRequest(
    @SerializedName("router_id") val routerId: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("uptime_limit") val uptimeLimit: Int? = null,
    @SerializedName("data_limit") val dataLimit: Long? = null, // Changed to Long for data
    @SerializedName("shared_users") val sharedUsers: Int? = null
)

data class HotspotPlan(
    @SerializedName("id") val id: String,
    @SerializedName("router_id") val routerId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("uptime_limit") val uptimeLimit: Long? = null,
    @SerializedName("data_limit") val dataLimit: Long? = null,
    @SerializedName("shared_users") val sharedUsers: Int = 1,
    @SerializedName("rate_limit") val rateLimit: String? = null
)

data class GenerateVouchersRequest(
    @SerializedName("router_id") val routerId: String,
    @SerializedName("plan_id") val planId: String,
    @SerializedName("count") val count: Int,
    @SerializedName("password_mode") val passwordMode: String,
    @SerializedName("length") val length: Int? = null
)

data class GenerateVouchersResponse(
    @SerializedName("vouchers_created") val vouchersCreated: Int,
    @SerializedName("synced_to_mikrotik") val syncedToMikrotik: Boolean,
    @SerializedName("sync_message") val syncMessage: String,
    @SerializedName("failed_users") val failedUsers: List<String>
)

data class VoucherResponse(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("plan_id") val planId: String? = null,
    @SerializedName("router_id") val routerId: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("first_use_at") val firstUseAt: String? = null,
    @SerializedName("validity_period") val validityPeriod: String? = null,
    @SerializedName("uptime_used") val uptimeUsed: Long? = null,
    @SerializedName("bytes_in") val bytesIn: Long? = null,
    @SerializedName("bytes_out") val bytesOut: Long? = null
)

data class VoucherPdfRequest(
    @SerializedName("router_id") val routerId: String,
    @SerializedName("vouchers") val vouchers: List<VoucherCredentials>,
    @SerializedName("include_qr") val includeQr: Boolean
)

data class VoucherCredentials(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class ActiveHotspotUser(
    @SerializedName("username") val user: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("uptime") val uptime: String? = null,
    @SerializedName("session_time_left") val sessionTimeLeft: String? = null,
    @SerializedName("bytes_in") val bytesIn: Long = 0,
    @SerializedName("bytes_out") val bytesOut: Long = 0,
    @SerializedName("bytes_total") val bytesTotal: Long = 0,
    // For UI compatibility
    val server: String? = null,
    val id: String? = null
)

data class HotspotUserResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("profile") val profile: String? = null,
    @SerializedName("limit_uptime") val limitUptime: String? = null,
    @SerializedName("limit_bytes_total") val limitBytesTotal: String? = null,
    @SerializedName("comment") val comment: String? = null
)

data class HotspotProfile(
    @SerializedName("name") val name: String? = null,
    @SerializedName("shared_users") val sharedUsers: String? = null,
    @SerializedName("rate_limit") val rateLimit: String? = null,
    @SerializedName("session_timeout") val sessionTimeout: String? = null,
    @SerializedName("idle_timeout") val idleTimeout: String? = null,
    @SerializedName("keepalive_timeout") val keepaliveTimeout: String? = null,
    val id: String? = null
)

data class ExecuteCommandRequest(
    @SerializedName("command") val command: String
)

data class ExecuteCommandResponse(
    @SerializedName("output") val output: Any // Can be JSON object or string
)

data class PPPoEPlan(
    @SerializedName("id") val id: String,
    @SerializedName("router_id") val routerId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("local_address") val localAddress: String? = null,
    @SerializedName("remote_address") val remoteAddress: String? = null,
    @SerializedName("rate_limit") val rateLimit: String? = null
)

data class CreatePPPoEPlanRequest(
    @SerializedName("router_id") val routerId: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("local_address") val localAddress: String,
    @SerializedName("remote_address") val remoteAddress: String,
    @SerializedName("rate_limit") val rateLimit: String
)

data class SyncPPPoEPlansRequest(
    @SerializedName("router_id") val routerId: String
)
