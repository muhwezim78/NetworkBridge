package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessageResponse(
    @SerializedName("id") val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("sender_role") val senderRole: String,
    @SerializedName("content") val content: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class SendMessageRequest(
    @SerializedName("conversation_id") val conversationId: String? = null,
    @SerializedName("content") val content: String
)

data class SendMessageResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("id") val id: String,
    @SerializedName("created_at") val createdAt: String
)
