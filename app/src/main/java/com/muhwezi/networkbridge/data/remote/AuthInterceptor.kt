package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.local.SessionManager
import com.muhwezi.networkbridge.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor that automatically adds JWT authentication token to API requests
 * and detects 401 responses to trigger session expiry.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get token synchronously (we're already in IO thread from OkHttp)
        val token = runBlocking {
            tokenManager.accessToken.first()
        }
        
        // If no token, proceed with original request (for login endpoint)
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // Add Authorization header with Bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        val response = chain.proceed(authenticatedRequest)
        
        // Detect expired/invalid token — but skip auth endpoints
        // (login/signup legitimately return 401 for wrong credentials)
        if (response.code == 401) {
            val path = originalRequest.url.encodedPath
            val isAuthEndpoint = path.contains("/login") || path.contains("/signup") || path.contains("/register")
            
            if (!isAuthEndpoint) {
                // Token is expired or invalid — clear it and notify the UI
                runBlocking { tokenManager.deleteTokens() }
                sessionManager.onSessionExpired()
            }
        }
        
        return response
    }
}
