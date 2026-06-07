package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val id: String,
    val title: String,
    val message: String,
    val level: String,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)

data class MarkReadResponse(
    val success: Boolean
)
