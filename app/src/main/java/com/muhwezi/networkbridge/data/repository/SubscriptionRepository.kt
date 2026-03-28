package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.GenerateTokenRequest
import com.muhwezi.networkbridge.data.model.GenerateTokenResponse
import com.muhwezi.networkbridge.data.model.RedeemTokenRequest
import com.muhwezi.networkbridge.data.model.SubscriptionStatus
import com.muhwezi.networkbridge.data.model.InitiateSubscriptionRequest
import com.muhwezi.networkbridge.data.model.InitiateSubscriptionResponse
import com.muhwezi.networkbridge.data.remote.SubscriptionService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionService: SubscriptionService
) {
    suspend fun getStatus(): Result<SubscriptionStatus> {
        return try {
            val response = subscriptionService.getStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun redeemToken(code: String, userId: String): Result<String> {
        return try {
            val response = subscriptionService.redeemToken(RedeemTokenRequest(code, userId))
            if (response.isSuccessful) {
                Result.success(response.body() ?: "Success")
            } else {
                Result.failure(Exception("Failed to redeem token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateToken(packageType: String, durationDays: Int): Result<GenerateTokenResponse> {
        return try {
            val request = GenerateTokenRequest(packageType, durationDays)
            val response = subscriptionService.generateToken(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initiateSubscription(packageType: String, phoneNumber: String): Result<InitiateSubscriptionResponse> {
        return try {
            val request = InitiateSubscriptionRequest(packageType, phoneNumber)
            val response = subscriptionService.initiateSubscription(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
