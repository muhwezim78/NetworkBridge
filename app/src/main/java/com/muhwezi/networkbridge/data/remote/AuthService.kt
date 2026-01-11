package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.LoginRequest
import com.muhwezi.networkbridge.data.model.LoginResponse
import com.muhwezi.networkbridge.data.model.SignupRequest
import com.muhwezi.networkbridge.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>
}
