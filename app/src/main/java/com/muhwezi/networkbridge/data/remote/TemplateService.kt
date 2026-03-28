package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TemplateService {
    @GET("templates")
    suspend fun getTemplates(@Query("type") type: String? = null): Response<List<Template>>

    @GET("routers/{router_id}/template")
    suspend fun getRouterTemplateConfig(@Path("router_id") routerId: String): Response<RouterTemplateConfig>

    @PUT("routers/{router_id}/template")
    suspend fun updateRouterTemplateConfig(
        @Path("router_id") routerId: String,
        @Body request: UpdateRouterTemplateRequest
    ): Response<RouterTemplateConfig>

    @Multipart
    @POST("storage/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<FileUploadResponse>

    // --- Admin ---

    @GET("admin/templates")
    suspend fun getAdminTemplates(): Response<List<Template>>

    @POST("admin/templates")
    suspend fun createTemplate(@Body request: CreateTemplateRequest): Response<Template>

    @PUT("admin/templates/{id}")
    suspend fun updateTemplate(
        @Path("id") id: String,
        @Body request: CreateTemplateRequest
    ): Response<Template>

    @DELETE("admin/templates/{id}")
    suspend fun deleteTemplate(@Path("id") id: String): Response<Unit>
}
