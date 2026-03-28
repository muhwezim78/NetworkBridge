package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response from GET /rest/system/identity on the local MikroTik router
 */
data class MikrotikIdentity(
    @SerializedName("name") val name: String
)

/**
 * Response from GET /rest/system/resource on the local MikroTik router
 */
data class MikrotikSystemResource(
    @SerializedName("board-name") val boardName: String? = null,
    @SerializedName("version") val version: String? = null,
    @SerializedName("uptime") val uptime: String? = null,
    @SerializedName("cpu-load") val cpuLoad: String? = null,
    @SerializedName("free-memory") val freeMemory: Long? = null,
    @SerializedName("total-memory") val totalMemory: Long? = null,
    @SerializedName("architecture-name") val architectureName: String? = null,
    @SerializedName("platform") val platform: String? = null
)

/**
 * Response from GET /rest/system/routerboard on the local MikroTik router
 */
data class MikrotikRouterboard(
    @SerializedName("serial-number") val serialNumber: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("firmware-type") val firmwareType: String? = null,
    @SerializedName("current-firmware") val currentFirmware: String? = null
)

/**
 * Response from GET /rest/ip/cloud on the local MikroTik router.
 * Provides the DDNS hostname assigned by MikroTik's IP Cloud service.
 * May be unavailable if IP Cloud is disabled or the device doesn't support it.
 */
data class MikrotikCloudInfo(
    @SerializedName("ddns-enabled") val ddnsEnabled: String? = null,
    @SerializedName("dns-name") val dnsName: String? = null,
    @SerializedName("public-address") val publicAddress: String? = null
)

/**
 * Aggregated information from a locally-connected MikroTik router
 */
data class LocalRouterInfo(
    val identity: MikrotikIdentity,
    val resource: MikrotikSystemResource,
    val routerboard: MikrotikRouterboard?,
    val cloudInfo: MikrotikCloudInfo? = null
)

/**
 * Connection parameters for a local MikroTik router
 */
data class LocalConnectionParams(
    val host: String,
    val port: Int = 443,
    val username: String,
    val password: String,
    val useSsl: Boolean = true
)
