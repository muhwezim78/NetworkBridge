package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.CreateRouterRequest
import com.muhwezi.networkbridge.data.model.Router
import com.muhwezi.networkbridge.data.remote.RouterService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouterRepository @Inject constructor(
    private val routerService: RouterService
) {
    suspend fun getRouters(): Result<List<Router>> {
        return try {
            val response = routerService.getRouters()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch routers: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRouter(id: String): Result<Router> {
        return try {
            val response = routerService.getRouter(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch router: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRouter(request: CreateRouterRequest): Result<Router> {
        return try {
            val response = routerService.createRouter(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "Router already exists"
                    else -> "Failed to create router: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRouter(id: String): Result<Unit> {
        return try {
            val response = routerService.deleteRouter(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete router: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
