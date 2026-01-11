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
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    // Add other fields if returned by GET /routers/{id}/hotspot/plans
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
    @SerializedName("code") val code: String,
    // Add other fields as needed
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
    @SerializedName("id") val id: String,
    @SerializedName("server") val server: String,
    @SerializedName("user") val user: String,
    @SerializedName("address") val address: String,
    @SerializedName("uptime") val uptime: String,
    // Add other fields
)

data class HotspotUserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("profile") val profile: String,
    // Add other fields
)

data class HotspotProfile(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    // Add other fields
)

data class ExecuteCommandRequest(
    @SerializedName("command") val command: String
)

data class ExecuteCommandResponse(
    @SerializedName("output") val output: Any // Can be JSON object or string
)
