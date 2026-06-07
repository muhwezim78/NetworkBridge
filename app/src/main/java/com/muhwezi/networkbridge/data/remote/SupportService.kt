package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.ChatMessageResponse
import com.muhwezi.networkbridge.data.model.SendMessageRequest
import com.muhwezi.networkbridge.data.model.SendMessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SupportService {
    @GET("support/messages")
    suspend fun getMyMessages(
        @Query("limit") limit: Int? = 100
    ): Response<List<ChatMessageResponse>>

    @POST("support/messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
}
