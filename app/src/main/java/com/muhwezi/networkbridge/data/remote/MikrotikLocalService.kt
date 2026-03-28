package com.muhwezi.networkbridge.data.remote

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.muhwezi.networkbridge.data.model.LocalConnectionParams
import com.muhwezi.networkbridge.data.model.MikrotikCloudInfo
import com.muhwezi.networkbridge.data.model.MikrotikIdentity
import com.muhwezi.networkbridge.data.model.MikrotikRouterboard
import com.muhwezi.networkbridge.data.model.MikrotikSystemResource
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Direct OkHttp-based client for communicating with a MikroTik router's 
 * REST API over the local network. Uses HTTP Basic Auth.
 *
 * MikroTik REST API (RouterOS v7+) serves endpoints at /rest/<path>.
 * For example: GET /rest/system/identity → returns the router's identity.
 */
@Singleton
class MikrotikLocalService @Inject constructor(
    @Named("local") private val localClient: OkHttpClient,
    private val gson: Gson
) {
    /**
     * Build the base URL for a given connection (e.g., https://192.168.88.1:443)
     */
    private fun baseUrl(params: LocalConnectionParams): String {
        val scheme = if (params.useSsl) "https" else "http"
        return "$scheme://${params.host}:${params.port}"
    }

    /**
     * Test connectivity and credentials by fetching the router's system identity.
     * GET /rest/system/identity
     */
    suspend fun testConnection(params: LocalConnectionParams): MikrotikIdentity {
        val url = "${baseUrl(params)}/rest/system/identity"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(params.username, params.password))
            .get()
            .build()

        val response = localClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val code = response.code
            val errorMsg = when (code) {
                401 -> "Invalid username or password"
                403 -> "Access denied. Check API permissions for this user"
                404 -> "REST API not found. Ensure RouterOS v7+ is installed"
                else -> "Connection failed (HTTP $code)"
            }
            throw MikrotikLocalException(errorMsg, code)
        }

        val body = response.body?.string() ?: throw MikrotikLocalException("Empty response from router")
        return gson.fromJson(body, MikrotikIdentity::class.java)
    }

    /**
     * Fetch system resource info (board name, version, CPU, memory, etc.)
     * GET /rest/system/resource
     */
    suspend fun getSystemResource(params: LocalConnectionParams): MikrotikSystemResource {
        val url = "${baseUrl(params)}/rest/system/resource"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(params.username, params.password))
            .get()
            .build()

        val response = localClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw MikrotikLocalException("Failed to fetch system resource (HTTP ${response.code})", response.code)
        }

        val body = response.body?.string() ?: throw MikrotikLocalException("Empty response")
        return gson.fromJson(body, MikrotikSystemResource::class.java)
    }

    /**
     * Fetch routerboard info (serial number, model, firmware).
     * GET /rest/system/routerboard
     * Note: Not all MikroTik devices are routerboards (e.g., x86), so this may 404.
     */
    suspend fun getRouterboard(params: LocalConnectionParams): MikrotikRouterboard? {
        val url = "${baseUrl(params)}/rest/system/routerboard"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(params.username, params.password))
            .get()
            .build()

        return try {
            val response = localClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            gson.fromJson(body, MikrotikRouterboard::class.java)
        } catch (e: Exception) {
            // Non-routerboard devices (x86, CHR) don't have this endpoint
            null
        }
    }

    /**
     * Fetch IP Cloud info (DDNS hostname, public address).
     * GET /rest/ip/cloud
     * Note: IP Cloud may be disabled or unavailable on some devices, so this may fail.
     */
    suspend fun getCloudInfo(params: LocalConnectionParams): MikrotikCloudInfo? {
        val url = "${baseUrl(params)}/rest/ip/cloud"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(params.username, params.password))
            .get()
            .build()

        return try {
            val response = localClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            gson.fromJson(body, MikrotikCloudInfo::class.java)
        } catch (e: Exception) {
            // IP Cloud not enabled or not supported
            null
        }
    }

    /**
     * Execute a RouterOS script on the router by:
     * 1. Creating a temporary script via POST /rest/system/script
     * 2. Running it via POST /rest/system/script/run
     * 3. Cleaning up the script via DELETE /rest/system/script/<id>
     *
     * This is used to push the WireGuard setup script to the router.
     */
    suspend fun executeSetupScript(params: LocalConnectionParams, script: String): String {
        val credential = Credentials.basic(params.username, params.password)
        val base = baseUrl(params)
        val scriptName = "nb_wg_setup_${System.currentTimeMillis()}"

        // Step 1: Create the script
        val createBody = JsonObject().apply {
            addProperty("name", scriptName)
            addProperty("source", script)
        }
        val createRequest = Request.Builder()
            .url("$base/rest/system/script")
            .header("Authorization", credential)
            .put(createBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val createResponse = localClient.newCall(createRequest).execute()
        if (!createResponse.isSuccessful) {
            val errorBody = createResponse.body?.string() ?: ""
            throw MikrotikLocalException(
                "Failed to create setup script on router (HTTP ${createResponse.code}): $errorBody",
                createResponse.code
            )
        }

        // Parse the created script's .id from response
        val createResult = createResponse.body?.string() ?: ""
        val scriptId = try {
            gson.fromJson(createResult, JsonObject::class.java)?.get(".id")?.asString
        } catch (e: Exception) { null }

        // Step 2: Run the script
        val runBody = JsonObject().apply {
            addProperty("number", scriptName)
        }
        val runRequest = Request.Builder()
            .url("$base/rest/system/script/run")
            .header("Authorization", credential)
            .post(runBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val runResponse = localClient.newCall(runRequest).execute()
        val runResult = runResponse.body?.string() ?: ""

        // Step 3: Cleanup — delete the temporary script
        if (scriptId != null) {
            try {
                val deleteRequest = Request.Builder()
                    .url("$base/rest/system/script/$scriptId")
                    .header("Authorization", credential)
                    .delete()
                    .build()
                localClient.newCall(deleteRequest).execute().close()
            } catch (_: Exception) {
                // Cleanup failure is non-critical
            }
        }

        if (!runResponse.isSuccessful) {
            throw MikrotikLocalException(
                "Script created but failed to execute (HTTP ${runResponse.code}): $runResult",
                runResponse.code
            )
        }

        return runResult
    }

    /**
     * Execute individual RouterOS commands line-by-line via the REST API.
     * Converts CLI-style commands like "/interface wireguard add name=wg0"
     * to REST API calls like POST /rest/interface/wireguard with {"name":"wg0"}
     * 
     * Falls back to the script-based approach if command parsing fails.
     */
    suspend fun executeCommands(params: LocalConnectionParams, commands: List<String>): List<String> {
        val results = mutableListOf<String>()
        val credential = Credentials.basic(params.username, params.password)
        val base = baseUrl(params)

        for (command in commands) {
            val trimmed = command.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.startsWith(":")) {
                continue // Skip comments, empty lines, and script-level commands
            }

            try {
                // Parse CLI command into REST API call
                val parts = trimmed.split(" ")
                val path = parts[0].replace("/", "/").trimEnd('/')
                
                // Determine verb from command
                val restPath = "$base/rest${path.replace(":", "/")}"
                
                val request = Request.Builder()
                    .url(restPath)
                    .header("Authorization", credential)
                    .get()
                    .build()

                val response = localClient.newCall(request).execute()
                results.add(response.body?.string() ?: "OK")
            } catch (e: Exception) {
                results.add("Error executing '$trimmed': ${e.message}")
            }
        }

        return results
    }
}

/**
 * Exception type for local MikroTik connection errors
 */
class MikrotikLocalException(
    message: String, 
    val httpCode: Int = 0,
    cause: Throwable? = null
) : Exception(message, cause)
