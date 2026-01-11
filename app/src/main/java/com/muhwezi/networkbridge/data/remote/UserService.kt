package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.UpdateUserRoleRequest
import com.muhwezi.networkbridge.data.model.UpdateUserStatusRequest
import com.muhwezi.networkbridge.data.model.UserItem
import com.muhwezi.networkbridge.data.model.UserListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @GET("users")
    suspend fun getUsers(): Response<UserListResponse>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserItem>

    @PUT("users/{id}/role")
    suspend fun updateUserRole(@Path("id") id: String, @Body request: UpdateUserRoleRequest): Response<Unit>

    @PUT("users/{id}/status")
    suspend fun updateUserStatus(@Path("id") id: String, @Body request: UpdateUserStatusRequest): Response<Unit>
}
