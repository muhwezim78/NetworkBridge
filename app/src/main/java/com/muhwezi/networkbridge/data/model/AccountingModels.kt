package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class IncomeReport(
    @SerializedName("period") val period: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("total_income") val totalIncome: Double = 0.0,
    @SerializedName("voucher_count") val voucherCount: Int = 0,
    // Legacy compat
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("count") val count: Int = 0
) {
    /** Unified accessor: use period if available, fall back to date */
    val displayDate: String get() = period ?: date ?: ""
    /** Unified accessor: use total_income if non-zero, fall back to amount */
    val displayAmount: Double get() = if (totalIncome != 0.0) totalIncome else amount
}

data class DashboardStats(
    @SerializedName("type") val type: String? = null,
    @SerializedName("total_routers") val totalRouters: Long = 0,
    @SerializedName("routers_online") val routersOnline: Long = 0,
    @SerializedName("active_vouchers") val activeVouchers: Long = 0,
    @SerializedName("total_revenue") val totalRevenue: Double = 0.0,
    @SerializedName("today_income") val todayIncome: Double = 0.0,
    @SerializedName("daily_activations") val dailyActivations: Long = 0,
    @SerializedName("income_change") val incomeChange: String = "",
    @SerializedName("routers_change") val routersChange: String = "",
    @SerializedName("low_balance_routers") val lowBalanceRouters: Long = 0,
    @SerializedName("income_chart") val incomeChart: List<ChartData>? = null,
    @SerializedName("voucher_distribution") val voucherDistribution: List<VoucherDistribution>? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)
