package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

// --- ISP Analytics ---

data class AnalyticsDashboardResponse(
    @SerializedName("summary") val summary: RevenueSummary,
    @SerializedName("daily_revenue") val dailyRevenue: List<DailyRevenueItem>,
    @SerializedName("top_plans") val topPlans: List<PlanPerformanceItem>,
    @SerializedName("peak_hours") val peakHours: List<PeakHourItem>
)

data class RevenueSummary(
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("total_transactions") val totalTransactions: Int,
    @SerializedName("successful_transactions") val successfulTransactions: Int,
    @SerializedName("failed_transactions") val failedTransactions: Int,
    @SerializedName("currency") val currency: String
)

data class DailyRevenueItem(
    @SerializedName("date") val date: String,
    @SerializedName("revenue") val revenue: Double,
    @SerializedName("transaction_count") val transactionCount: Int
)

data class PlanPerformanceItem(
    @SerializedName("plan_id") val planId: String,
    @SerializedName("plan_name") val planName: String,
    @SerializedName("sales_count") val salesCount: Int,
    @SerializedName("total_revenue") val totalRevenue: Double
)

data class PeakHourItem(
    @SerializedName("hour") val hour: Int,
    @SerializedName("transaction_count") val transactionCount: Int,
    @SerializedName("revenue") val revenue: Double
)

data class RouterPerformanceItem(
    @SerializedName("router_id") val routerId: String,
    @SerializedName("router_name") val routerName: String,
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("transaction_count") val transactionCount: Int,
    @SerializedName("active_vouchers") val activeVouchers: Int
)

// --- Admin Analytics ---

data class AdminRevenueSummary(
    @SerializedName("total_subscription_income") val subscriptionIncome: Double,
    @SerializedName("subscription_count") val subscriptionCount: Int,
    @SerializedName("total_commission_income") val commissionIncome: Double,
    @SerializedName("total_platform_revenue") val totalRevenue: Double,
    @SerializedName("this_month_revenue") val thisMonthRevenue: Double,
    @SerializedName("last_month_revenue") val lastMonthRevenue: Double,
    @SerializedName("growth_percentage") val growthPercentage: Double,
    @SerializedName("basic_income") val basicIncome: Double,
    @SerializedName("premium_income") val premiumIncome: Double,
    @SerializedName("enterprise_income") val enterpriseIncome: Double
)

data class AdminRevenueTimelineItem(
    @SerializedName("period") val period: String,
    @SerializedName("subscription_income") val subscriptionIncome: Double,
    @SerializedName("commission_income") val commissionIncome: Double,
    @SerializedName("total") val total: Double
)

data class IspPerformanceItem(
    @SerializedName("user_id") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("total_voucher_sales") val totalVoucherSales: Double,
    @SerializedName("commission_earned") val commissionEarned: Double,
    @SerializedName("voucher_count") val voucherCount: Int,
    @SerializedName("active_users") val activeUsers: Int,
    @SerializedName("commission_rate") val commissionRate: Double
)

data class DailyActivationItem(
    @SerializedName("date") val date: String,
    @SerializedName("online_count") val onlineCount: Int,
    @SerializedName("cash_count") val cashCount: Int,
    @SerializedName("total_count") val totalCount: Int
)
