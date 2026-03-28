package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.*
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
    suspend fun adminListRouters(): Response<List<Router>>

    @GET("routers/{id}/logs")
    suspend fun getRouterLogs(@Path("id") routerId: String): Response<List<RouterLog>>

    @GET("routers/{id}/backups")
    suspend fun getRouterBackups(@Path("id") routerId: String): Response<List<RouterBackup>>

    @GET("routers/{id}/pools")
    suspend fun getIPPools(@Path("id") routerId: String): Response<List<IPPool>>

    @GET("routers/{id}/traffic/stats")
    suspend fun getTrafficStats(@Path("id") routerId: String): Response<List<TrafficStat>>

    @POST("routers/{id}/traffic/setup")
    suspend fun setupTrafficFlow(@Path("id") routerId: String): Response<Unit>

    @POST("routers/{id}/logs/setup")
    suspend fun setupRemoteLogging(@Path("id") routerId: String): Response<Unit>

    @POST("routers/{id}/reboot")
    suspend fun rebootRouter(@Path("id") routerId: String): Response<Map<String, String>>

    @POST("routers/{id}/shutdown")
    suspend fun shutdownRouter(@Path("id") routerId: String): Response<Map<String, String>>

    @GET("routers/{id}/interfaces")
    suspend fun getInterfaces(@Path("id") routerId: String): Response<List<RouterInterface>>

    @GET("routers/{id}/health")
    suspend fun getHealth(@Path("id") routerId: String): Response<List<RouterHealth>>

    @GET("routers/{id}/scripts")
    suspend fun getScripts(@Path("id") routerId: String): Response<List<RouterScript>>

    @GET("routers/{id}/scheduler")
    suspend fun getScheduler(@Path("id") routerId: String): Response<List<RouterJob>>

    @POST("routers/{id}/backups")
    suspend fun createBackup(@Path("id") routerId: String): Response<Unit>

    @POST("routers/{id}/walled-garden")
    suspend fun configureWalledGarden(@Path("id") routerId: String): Response<Map<String, String>>
}
