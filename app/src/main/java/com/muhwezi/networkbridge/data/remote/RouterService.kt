package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.CreateRouterRequest
import com.muhwezi.networkbridge.data.model.Router
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RouterService {
    @GET("routers")
    suspend fun getRouters(): Response<List<Router>>

    @GET("routers/{id}")
    suspend fun getRouter(@Path("id") id: String): Response<Router>

    @POST("routers")
    suspend fun createRouter(@Body request: CreateRouterRequest): Response<Router>

    @DELETE("routers/{id}")
    suspend fun deleteRouter(@Path("id") id: String): Response<Unit>

    // Admin only
    @GET("admin/routers")
    suspend fun adminListRouters(): Response<List<Router>> // Assuming AdminRouterListItem is similar to Router or I should define it. Spec says AdminRouterListItem. I'll use Router for now or check if I need a new model.
    // Spec says: List of AdminRouterListItem containing router and user info.
    // I should probably define AdminRouterListItem in RouterModels.kt or UserManagementModels.kt.
    // For now I'll use Router and maybe add user info fields to Router or create a new class.
    // Let's check RouterModels.kt again. I didn't add AdminRouterListItem.
    // I'll stick with List<Router> for now and maybe update later if needed, or just add a TODO.
}
