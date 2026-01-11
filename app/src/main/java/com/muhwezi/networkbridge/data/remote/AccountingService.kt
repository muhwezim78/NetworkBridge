package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.DashboardStats
import com.muhwezi.networkbridge.data.model.IncomeReport
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AccountingService {
    @GET("reports/revenue")
    suspend fun getRevenueReport(
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?,
        @Query("interval") interval: String?,
        @Query("limit") limit: Int?,
        @Query("router_id") routerId: String?
    ): Response<List<IncomeReport>>

    @GET("dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStats> // Or WebSocketEvent if it returns that structure
}
