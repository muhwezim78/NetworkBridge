package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class VpnProvisionRequest(
    @SerializedName("wg_public_key") val wgPublicKey: String
)

data class VpnProvisionResponse(
    @SerializedName("phone_ip")         val phoneIp: String,
    @SerializedName("server_public_key") val serverPublicKey: String,
    @SerializedName("server_endpoint")  val serverEndpoint: String,
    @SerializedName("allowed_ips")      val allowedIps: String
)

sealed class VpnState {
    object Idle : VpnState()
    object Connecting : VpnState()
    data class Connected(val phoneIp: String, val routerCount: Int) : VpnState()
    data class Error(val message: String) : VpnState()
}
