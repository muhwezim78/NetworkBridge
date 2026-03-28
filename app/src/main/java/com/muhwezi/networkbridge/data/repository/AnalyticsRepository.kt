package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.*
import com.muhwezi.networkbridge.data.remote.AnalyticsService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val analyticsService: AnalyticsService
) {
    suspend fun getDashboardOverview(routerId: String? = null): Result<AnalyticsDashboardResponse> {
        return try {
            val response = analyticsService.getDashboardOverview(routerId = routerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlanPerformance(): Result<List<PlanPerformanceItem>> {
        return try {
            val response = analyticsService.getPlanPerformance()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPeakHours(): Result<List<PeakHourItem>> {
        return try {
            val response = analyticsService.getPeakHours()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
