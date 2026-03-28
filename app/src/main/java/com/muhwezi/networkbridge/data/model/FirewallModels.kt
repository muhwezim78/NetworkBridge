package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class AddGlobalFirewallRequest(
    @SerializedName("action") val action: String,
    @SerializedName("chain") val chain: String,
    @SerializedName("src_address") val srcAddress: String? = null,
    @SerializedName("dst_address") val dstAddress: String? = null,
    @SerializedName("protocol") val protocol: String? = null,
    @SerializedName("dst_port") val dstPort: String? = null,
    @SerializedName("comment") val comment: String? = null
)

data class GlobalFirewallAddress(
    @SerializedName("id") val id: String,
    @SerializedName("action") val action: String? = null,
    @SerializedName("chain") val chain: String? = null,
    @SerializedName("src_address") val srcAddress: String? = null,
    @SerializedName("dst_address") val dstAddress: String? = null,
    @SerializedName("protocol") val protocol: String? = null,
    @SerializedName("dst_port") val dstPort: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
