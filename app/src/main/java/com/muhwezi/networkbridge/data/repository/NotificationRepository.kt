package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.UserSmsLogResponse
import com.muhwezi.networkbridge.data.model.UserSmsStatsResponse
import com.muhwezi.networkbridge.data.model.NotificationResponse
import com.muhwezi.networkbridge.data.model.MarkReadResponse
import com.muhwezi.networkbridge.data.remote.NotificationService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val api: NotificationService
) {
    suspend fun getSmsLogs(limit: Int = 100): Result<List<UserSmsLogResponse>> {
        return try {
            val response = api.getMySmsLogs(limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch SMS logs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSmsStats(): Result<UserSmsStatsResponse> {
        return try {
            val response = api.getMySmsStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch SMS stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(limit: Int = 50): Result<List<NotificationResponse>> {
        return try {
            val response = api.getNotifications(limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch notifications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(id: String): Result<Boolean> {
        return try {
            val response = api.markNotificationRead(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to mark read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsRead(): Result<Boolean> {
        return try {
            val response = api.markAllNotificationsRead()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to mark all read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
