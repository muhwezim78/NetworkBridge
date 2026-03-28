package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AnalyticsService {
    // --- ISP ---

    @GET("analytics/dashboard")
    suspend fun getDashboardOverview(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("router_id") routerId: String? = null
    ): Response<AnalyticsDashboardResponse>

    @GET("analytics/revenue")
    suspend fun getRevenueSummary(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("router_id") routerId: String? = null
    ): Response<RevenueSummary>

    @GET("analytics/daily")
    suspend fun getDailyRevenue(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("router_id") routerId: String? = null
    ): Response<List<DailyRevenueItem>>

    @GET("analytics/daily-activations")
    suspend fun getDailyActivations(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("router_id") routerId: String? = null
    ): Response<List<DailyActivationItem>>

    @GET("analytics/plans")
    suspend fun getPlanPerformance(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<List<PlanPerformanceItem>>

    @GET("analytics/peak-hours")
    suspend fun getPeakHours(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<List<PeakHourItem>>

    @GET("analytics/routers")
    suspend fun getRouterPerformance(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<List<RouterPerformanceItem>>

    // --- Admin ---

    @GET("admin/analytics/revenue/summary")
    suspend fun getAdminRevenueSummary(): Response<AdminRevenueSummary>

    @GET("admin/analytics/revenue/timeline")
    suspend fun getAdminRevenueTimeline(
        @Query("interval") interval: String? = "day",
        @Query("limit") limit: Int? = 30
    ): Response<List<AdminRevenueTimelineItem>>

    @GET("admin/analytics/isps/performance")
    suspend fun getIspPerformance(): Response<List<IspPerformanceItem>>
}
