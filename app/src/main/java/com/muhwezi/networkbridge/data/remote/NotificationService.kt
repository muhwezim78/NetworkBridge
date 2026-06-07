package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.UserSmsLogResponse
import com.muhwezi.networkbridge.data.model.UserSmsStatsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import com.muhwezi.networkbridge.data.model.NotificationResponse
import com.muhwezi.networkbridge.data.model.MarkReadResponse

interface NotificationService {
    @GET("user/sms-logs")
    suspend fun getMySmsLogs(
        @Query("limit") limit: Int? = 100
    ): Response<List<UserSmsLogResponse>>

    @GET("user/sms-stats")
    suspend fun getMySmsStats(): Response<UserSmsStatsResponse>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int? = 50
    ): Response<List<NotificationResponse>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(
        @Path("id") id: String
    ): Response<MarkReadResponse>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<MarkReadResponse>
}
