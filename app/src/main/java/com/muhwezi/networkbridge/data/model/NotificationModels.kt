package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class UserSmsLogResponse(
    @SerializedName("id") val id: String,
    @SerializedName("recipient") val recipient: String,
    @SerializedName("message") val message: String,
    @SerializedName("sms_type") val smsType: String,
    @SerializedName("status") val status: String?,
    @SerializedName("cost") val cost: String?,
    @SerializedName("created_at") val createdAt: String
)

data class SmsTimelinePoint(
    @SerializedName("date") val date: String?,
    @SerializedName("count") val count: Long,
    @SerializedName("cost") val cost: Double
)

data class UserSmsStatsResponse(
    @SerializedName("total_sent") val totalSent: Long,
    @SerializedName("total_failed") val totalFailed: Long,
    @SerializedName("total_cost") val totalCost: Double,
    @SerializedName("timeline") val timeline: List<SmsTimelinePoint>
)
