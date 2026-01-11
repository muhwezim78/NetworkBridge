package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class UserItem(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class UpdateUserRoleRequest(
    @SerializedName("role") val role: String
)

data class UpdateUserStatusRequest(
    @SerializedName("is_active") val isActive: Boolean
)

data class UserListResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("users") val users: List<UserItem>
)
