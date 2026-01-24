package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

/**
 * Common data model for WebSocket events from the backend.
 * Backend uses a "type" tag to distinguish events.
 */
sealed class WebSocketEvent {
    abstract val type: String

    data class RouterStatus(
        @SerializedName("type") override val type: String = "router_status",
        @SerializedName("router_id") val routerId: String,
        @SerializedName("router_name") val routerName: String,
        @SerializedName("status") val status: String,
        @SerializedName("timestamp") val timestamp: String
    ) : WebSocketEvent()

    data class VoucherActivated(
        @SerializedName("type") override val type: String = "voucher_activated",
        @SerializedName("voucher_id") val voucherId: String,
        @SerializedName("code") val code: String,
        @SerializedName("plan_name") val planName: String,
        @SerializedName("price") val price: Double,
        @SerializedName("timestamp") val timestamp: String
    ) : WebSocketEvent()

    data class IncomeRecorded(
        @SerializedName("type") override val type: String = "income_recorded",
        @SerializedName("amount") val amount: Double,
        @SerializedName("plan_name") val planName: String,
        @SerializedName("voucher_code") val voucherCode: String,
        @SerializedName("timestamp") val timestamp: String
    ) : WebSocketEvent()

    data class DashboardStats(
        @SerializedName("type") override val type: String = "dashboard_stats",
        @SerializedName("total_routers") val totalRouters: Long,
        @SerializedName("routers_online") val routersOnline: Long,
        @SerializedName("active_vouchers") val activeVouchers: Long,
        @SerializedName("total_revenue") val totalRevenue: Double,
        @SerializedName("today_income") val todayIncome: Double,
        @SerializedName("income_change") val incomeChange: String,
        @SerializedName("routers_change") val routersChange: String,
        @SerializedName("income_chart") val incomeChart: List<ChartData>,
        @SerializedName("voucher_distribution") val voucherDistribution: List<VoucherDistribution>,
        @SerializedName("timestamp") val timestamp: String
    ) : WebSocketEvent()
}

data class ChartData(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: Double
)

data class VoucherDistribution(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: Long,
    @SerializedName("color") val color: String
)
