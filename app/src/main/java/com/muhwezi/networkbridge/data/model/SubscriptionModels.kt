package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class SubscriptionStatus(
    @SerializedName("package_type") val packageType: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("expires_at") val expiresAt: String,
    @SerializedName("router_limit") val routerLimit: Int,
    @SerializedName("routers_used") val routersUsed: Int
)

data class RedeemTokenRequest(
    @SerializedName("code") val code: String,
    @SerializedName("user_id") val userId: String
)

data class GenerateTokenRequest(
    @SerializedName("package_type") val packageType: String,
    @SerializedName("duration_days") val durationDays: Int
)

data class GenerateTokenResponse(
    @SerializedName("code") val code: String
)
