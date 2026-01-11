package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.UpdateUserRoleRequest
import com.muhwezi.networkbridge.data.model.UpdateUserStatusRequest
import com.muhwezi.networkbridge.data.model.UserItem
import com.muhwezi.networkbridge.data.model.UserListResponse
import com.muhwezi.networkbridge.data.remote.UserService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userService: UserService
) {
    suspend fun getUsers(): Result<UserListResponse> {
        return try {
            val response = userService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch users: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(id: String): Result<UserItem> {
        return try {
            val response = userService.getUser(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(id: String, role: String): Result<Unit> {
        return try {
            val response = userService.updateUserRole(id, UpdateUserRoleRequest(role))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update user role: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserStatus(id: String, isActive: Boolean): Result<Unit> {
        return try {
            val response = userService.updateUserStatus(id, UpdateUserStatusRequest(isActive))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update user status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
