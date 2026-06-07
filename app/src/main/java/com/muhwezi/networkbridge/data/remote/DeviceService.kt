package com.muhwezi.networkbridge.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT

data class FcmTokenRequest(val fcm_token: String)

interface DeviceService {
    @PUT("device/fcm-token")
    suspend fun registerFcmToken(@Body body: FcmTokenRequest): Response<Unit>

    @DELETE("device/fcm-token")
    suspend fun clearFcmToken(): Response<Unit>
}
