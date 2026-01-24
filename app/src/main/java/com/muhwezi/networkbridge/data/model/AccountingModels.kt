package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class IncomeReport(
    @SerializedName("date") val date: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("count") val count: Int
)
data class DashboardStats(
    @SerializedName("total_users") val totalUsers: Int,
    @SerializedName("active_routers") val activeRouters: Int,
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("active_subscriptions") val activeSubscriptions: Int
)
