package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.DashboardStats
import com.muhwezi.networkbridge.data.model.IncomeReport
import com.muhwezi.networkbridge.data.remote.AccountingService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountingRepository @Inject constructor(
    private val accountingService: AccountingService
) {
    suspend fun getRevenueReport(
        startDate: String? = null,
        endDate: String? = null,
        interval: String? = null,
        limit: Int? = null,
        routerId: String? = null
    ): Result<List<IncomeReport>> {
        return try {
            val response = accountingService.getRevenueReport(startDate, endDate, interval, limit, routerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch revenue report: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            val response = accountingService.getDashboardStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch dashboard stats: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
