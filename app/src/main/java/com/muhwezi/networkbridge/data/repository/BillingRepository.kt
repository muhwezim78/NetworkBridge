package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.*
import com.muhwezi.networkbridge.data.remote.BillingService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val billingService: BillingService
) {
    suspend fun getWallet(): Result<WalletResponse> {
        return try {
            val response = billingService.getWallet()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(): Result<List<Transaction>> {
        return try {
            val response = billingService.getTransactions()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestWithdrawal(amount: Double, phoneNumber: String): Result<WithdrawalResponse> {
        return try {
            val request = WithdrawRequest(amount, phoneNumber)
            val response = billingService.requestWithdrawal(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWithdrawals(): Result<List<WithdrawalHistoryItem>> {
        return try {
            val response = billingService.getWithdrawals()
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
