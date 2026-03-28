package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface BillingService {
    @GET("billing/wallet")
    suspend fun getWallet(): Response<WalletResponse>

    @GET("billing/transactions")
    suspend fun getTransactions(): Response<List<Transaction>>

    @POST("billing/withdraw")
    suspend fun requestWithdrawal(@Body request: WithdrawRequest): Response<WithdrawalResponse>

    @GET("billing/withdrawals")
    suspend fun getWithdrawals(): Response<List<WithdrawalHistoryItem>>

    // --- Admin ---

    @GET("admin/commissions")
    suspend fun getCommissions(): Response<List<CommissionRate>>

    @PUT("admin/commissions/{user_id}")
    suspend fun updateCommission(
        @Path("user_id") userId: String,
        @Body request: UpdateCommissionRequest
    ): Response<Unit>

    @GET("admin/platform-wallet")
    suspend fun getPlatformWallet(): Response<PlatformWalletResponse>

    @GET("admin/commissions/history")
    suspend fun getCommissionsHistory(): Response<List<CommissionHistoryItem>>
}
