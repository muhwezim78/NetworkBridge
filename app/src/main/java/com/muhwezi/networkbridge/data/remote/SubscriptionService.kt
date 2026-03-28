package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SubscriptionService {
    @GET("subscription/status")
    suspend fun getStatus(): Response<SubscriptionStatus>

    @POST("subscription/redeem")
    suspend fun redeemToken(@Body request: RedeemTokenRequest): Response<String> // Or Response<Unit> if no content

    @POST("admin/generate_token")
    suspend fun generateToken(@Body request: GenerateTokenRequest): Response<GenerateTokenResponse>

    @POST("subscription/initiate")
    suspend fun initiateSubscription(@Body request: InitiateSubscriptionRequest): Response<InitiateSubscriptionResponse>
}
