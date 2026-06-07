package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.ChatMessageResponse
import com.muhwezi.networkbridge.data.model.SendMessageRequest
import com.muhwezi.networkbridge.data.model.SendMessageResponse
import com.muhwezi.networkbridge.data.remote.SupportService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportRepository @Inject constructor(
    private val api: SupportService
) {
    suspend fun getMessages(limit: Int = 100): Result<List<ChatMessageResponse>> {
        return try {
            val response = api.getMyMessages(limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(content: String): Result<SendMessageResponse> {
        return try {
            val request = SendMessageRequest(content = content)
            val response = api.sendMessage(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
