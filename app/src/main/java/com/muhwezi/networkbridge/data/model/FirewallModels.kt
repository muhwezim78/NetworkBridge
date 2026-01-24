package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class AddGlobalFirewallRequest(
    @SerializedName("list_name") val listName: String,
    @SerializedName("address") val address: String,
    @SerializedName("comment") val comment: String
)

data class GlobalFirewallAddress(
    @SerializedName("id") val id: String,
    @SerializedName("list_name") val listName: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
