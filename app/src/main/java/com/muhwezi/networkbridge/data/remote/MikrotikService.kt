package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MikrotikService {
    @POST("mikrotik/pppoe")
    suspend fun createPPPoEUser(@Body request: PPPoEUserRequest): Response<Unit> // Success message

    @POST("mikrotik/hotspot/plans")
    suspend fun createHotspotPlan(@Body request: CreateHotspotPlanRequest): Response<String> // UUID

    @GET("routers/{id}/hotspot/plans")
    suspend fun getHotspotPlans(@Path("id") routerId: String): Response<List<HotspotPlan>>

    @POST("routers/{id}/hotspot/plans/sync")
    suspend fun syncHotspotPlans(@Path("id") routerId: String): Response<Unit>

    @POST("mikrotik/hotspot/vouchers")
    suspend fun generateVouchers(@Body request: GenerateVouchersRequest): Response<GenerateVouchersResponse>

    @GET("mikrotik/hotspot/vouchers")
    suspend fun getVouchers(@Query("router_id") routerId: String?): Response<List<VoucherResponse>>

    @POST("mikrotik/hotspot/vouchers/pdf")
    suspend fun generateVoucherPdf(@Body request: VoucherPdfRequest): Response<ResponseBody> // Binary PDF

    @GET("routers/{id}/hotspot/active")
    suspend fun getActiveHotspotUsers(@Path("id") routerId: String): Response<List<ActiveHotspotUser>>

    @GET("routers/{id}/hotspot/users")
    suspend fun getHotspotUsers(@Path("id") routerId: String): Response<List<HotspotUserResponse>>

    @GET("routers/{id}/hotspot/profiles")
    suspend fun getHotspotProfiles(@Path("id") routerId: String): Response<List<HotspotProfile>>

    @POST("routers/{id}/execute")
    suspend fun executeCommand(@Path("id") routerId: String, @Body request: ExecuteCommandRequest): Response<ExecuteCommandResponse>

    @POST("mikrotik/pppoe/plans")
    suspend fun createPPPoEPlan(@Body request: CreatePPPoEPlanRequest): Response<Unit>

    @POST("mikrotik/pppoe/plans/sync")
    suspend fun syncPPPoEPlans(@Body request: SyncPPPoEPlansRequest): Response<Unit>

    @GET("routers/{id}/pppoe/plans")
    suspend fun getPPPoEPlans(@Path("id") routerId: String): Response<List<PPPoEPlan>>
}
