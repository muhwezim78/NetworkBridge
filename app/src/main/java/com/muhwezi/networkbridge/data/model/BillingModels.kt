package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class WalletResponse(
    @SerializedName("balance") val balance: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("total_earned") val totalEarned: Double,
    @SerializedName("commission_rate") val commissionRate: Double
)

data class Transaction(
    @SerializedName("id") val id: String,
    @SerializedName("invoice_id") val invoiceId: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("status") val status: String, // completed, pending, failed
    @SerializedName("transaction_type") val transactionType: String, // voucher_sale, subscription
    @SerializedName("created_at") val createdAt: String
)

data class WithdrawRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("phone_number") val phoneNumber: String
)

data class WithdrawalResponse(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String, // pending
    @SerializedName("message") val message: String
)

data class WithdrawalHistoryItem(
    @SerializedName("id") val id: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String
)

// --- Admin Models ---

data class CommissionRate(
    @SerializedName("user_id") val userId: String,
    @SerializedName("email") val email: String,
    @SerializedName("commission_rate") val commissionRate: Double,
    @SerializedName("balance") val balance: Double
)

data class UpdateCommissionRequest(
    @SerializedName("commission_rate") val commissionRate: Double
)

data class PlatformWalletResponse(
    @SerializedName("balance") val balance: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("total_earned") val totalEarned: Double
)

data class CommissionHistoryItem(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("old_rate") val oldRate: Double? = null,
    @SerializedName("new_rate") val newRate: Double? = null,
    @SerializedName("changed_by") val changedBy: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
