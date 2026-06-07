package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.LoyaltySettingsResponse
import com.muhwezi.networkbridge.data.model.UpdateLoyaltySettingsRequest
import com.muhwezi.networkbridge.data.remote.BillingService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoyaltyRepository @Inject constructor(
    private val api: BillingService
) {
    suspend fun getLoyaltySettings(routerId: String): Result<LoyaltySettingsResponse> {
        return try {
            val response = api.getLoyaltySettings(routerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch loyalty settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLoyaltySettings(
        routerId: String,
        enabled: Boolean,
        rewardType: String,
        rewardValue: String,
        minPurchase: Double?
    ): Result<Unit> {
        return try {
            val request = UpdateLoyaltySettingsRequest(
                programEnabled = enabled,
                rewardType = rewardType,
                rewardValue = rewardValue,
                minPurchaseRequired = minPurchase
            )
            val response = api.updateLoyaltySettings(routerId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to update loyalty settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
