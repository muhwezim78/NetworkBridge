package com.muhwezi.networkbridge.data.repository

import android.util.Base64
import com.muhwezi.networkbridge.data.local.TokenManager
import com.muhwezi.networkbridge.data.model.LoginRequest
import com.muhwezi.networkbridge.data.remote.AuthService
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) {
    val authToken: Flow<String?> = tokenManager.accessToken

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = authService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                // Try explicit userId first, then extract from JWT sub claim
                val userId = body.userId ?: extractUserIdFromJwt(body.accessToken)
                userId?.let { tokenManager.saveUserId(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception(handleError(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    suspend fun signup(request: com.muhwezi.networkbridge.data.model.SignupRequest): Result<Unit> {
        return try {
            val response = authService.signup(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                val userId = body.userId ?: extractUserIdFromJwt(body.accessToken)
                userId?.let { tokenManager.saveUserId(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception(handleError(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    suspend fun getCurrentUser(): Result<com.muhwezi.networkbridge.data.model.UserResponse> {
        return try {
            val response = authService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(handleError(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    /**
     * Extract the user ID from the JWT token's "sub" claim.
     * JWT format: header.payload.signature — we decode the payload (base64url).
     */
    private fun extractUserIdFromJwt(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            JSONObject(payload).optString("sub", null)
        } catch (e: Exception) {
            null
        }
    }

    private fun handleError(code: Int): String {
        return when (code) {
            401 -> "Unauthorized"
            403 -> "Access forbidden"
            404 -> "Not found"
            500 -> "Server error. Please try again later"
            503 -> "Service unavailable. Please try again later"
            else -> "Error: $code"
        }
    }

    private fun handleException(e: Exception): Exception {
        return when (e) {
            is java.net.UnknownHostException -> Exception("Cannot connect to server. Please check your internet connection")
            is java.net.SocketTimeoutException -> Exception("Connection timeout. Please try again")
            else -> Exception("Network error: ${e.message}")
        }
    }

    suspend fun logout() {
        tokenManager.deleteTokens()
    }
}
