package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.LocalConnectionParams
import com.muhwezi.networkbridge.data.model.LocalRouterInfo
import com.muhwezi.networkbridge.data.model.MikrotikIdentity
import com.muhwezi.networkbridge.data.remote.MikrotikLocalException
import com.muhwezi.networkbridge.data.remote.MikrotikLocalService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for local MikroTik router operations.
 * Wraps MikrotikLocalService with proper error handling and runs 
 * network calls on IO dispatcher.
 */
@Singleton
class MikrotikLocalRepository @Inject constructor(
    private val localService: MikrotikLocalService
) {
    /**
     * Test connection to a MikroTik router on the local network.
     * Verifies credentials by fetching the router's system identity.
     */
    suspend fun verifyConnection(params: LocalConnectionParams): Result<MikrotikIdentity> {
        return withContext(Dispatchers.IO) {
            try {
                val identity = localService.testConnection(params)
                Result.success(identity)
            } catch (e: MikrotikLocalException) {
                Result.failure(e)
            } catch (e: javax.net.ssl.SSLHandshakeException) {
                Result.failure(Exception("SSL certificate error. The router's certificate could not be verified."))
            } catch (e: java.net.ConnectException) {
                Result.failure(Exception("Cannot connect to ${params.host}:${params.port}. Ensure you are on the same network and the REST API is enabled."))
            } catch (e: java.net.SocketTimeoutException) {
                if (params.port == 8728) {
                    Result.failure(Exception("Connection timed out. Port 8728 is for the raw API, but this app uses the REST API (usually port 443 or 80). Please change the port."))
                } else {
                    Result.failure(Exception("Connection timed out on ${params.host}:${params.port}. Check the IP address and ensure the router is reachable."))
                }
            } catch (e: java.net.UnknownHostException) {
                Result.failure(Exception("Cannot resolve host '${params.host}'. Check the IP address."))
            } catch (e: Exception) {
                Result.failure(Exception("Connection failed: ${e.message}"))
            }
        }
    }

    /**
     * Fetch comprehensive router info: identity, system resource, and routerboard.
     * Should be called after verifyConnection succeeds.
     */
    suspend fun fetchRouterInfo(params: LocalConnectionParams): Result<LocalRouterInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val identity = localService.testConnection(params)
                val resource = localService.getSystemResource(params)
                val routerboard = localService.getRouterboard(params) // may be null for non-routerboard devices
                val cloudInfo = localService.getCloudInfo(params) // may be null if IP Cloud is disabled

                Result.success(
                    LocalRouterInfo(
                        identity = identity,
                        resource = resource,
                        routerboard = routerboard,
                        cloudInfo = cloudInfo
                    )
                )
            } catch (e: MikrotikLocalException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to fetch router info: ${e.message}"))
            }
        }
    }

    /**
     * Push and execute the WireGuard setup script on the router.
     * The script is executed as a temporary RouterOS script which is cleaned up afterwards.
     */
    suspend fun pushSetupScript(params: LocalConnectionParams, script: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate script is not empty
                if (script.isBlank()) {
                    return@withContext Result.failure(Exception("Setup script is empty"))
                }

                val result = localService.executeSetupScript(params, script)
                Result.success(result)
            } catch (e: MikrotikLocalException) {
                val userMessage = when {
                    e.httpCode == 401 -> "Authentication failed. Credentials may have changed."
                    e.httpCode == 400 -> "Script contains invalid commands: ${e.message}"
                    else -> "Failed to execute setup script: ${e.message}"
                }
                Result.failure(Exception(userMessage))
            } catch (e: java.net.SocketTimeoutException) {
                if (params.port == 8728) {
                    Result.failure(Exception("Connection timed out. Port 8728 is for the raw API, but this app uses the REST API (usually port 443 or 80). Please change the port."))
                } else {
                    Result.failure(Exception("Connection timed out on ${params.host}:${params.port}. Check the IP address and ensure the router is reachable."))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to push setup script: ${e.message}"))
            }
        }
    }
}
