package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class IncomeReport(
    @SerializedName("date") val date: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("count") val count: Int
)

data class DashboardStats(
    @SerializedName("total_routers") val totalRouters: Int,
    @SerializedName("connected_routers") val connectedRouters: Int,
    @SerializedName("total_active_users") val totalActiveUsers: Int,
    @SerializedName("total_revenue_today") val totalRevenueToday: Double
)
