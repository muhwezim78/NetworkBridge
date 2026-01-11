package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.local.TokenManager
import com.muhwezi.networkbridge.data.model.LoginRequest
import com.muhwezi.networkbridge.data.remote.AuthService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) {
    val authToken: Flow<String?> = tokenManager.token

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = authService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                tokenManager.saveToken(response.body()!!.token)
                response.body()!!.userId?.let { tokenManager.saveUserId(it) }
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
                tokenManager.saveToken(response.body()!!.token)
                response.body()!!.userId?.let { tokenManager.saveUserId(it) }
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
        tokenManager.deleteToken()
    }
}
