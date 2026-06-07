package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.LoginRequest
import com.muhwezi.networkbridge.data.model.LoginResponse
import com.muhwezi.networkbridge.data.model.SignupRequest
import com.muhwezi.networkbridge.data.model.UserResponse
import com.muhwezi.networkbridge.data.model.UpdateProfileRequest
import com.muhwezi.networkbridge.data.model.ChangePasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<Unit>

    @PUT("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>
}

