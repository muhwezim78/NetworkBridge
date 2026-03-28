package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.*
import com.muhwezi.networkbridge.data.remote.TemplateService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val templateService: TemplateService
) {
    suspend fun getTemplates(type: String? = null): Result<List<Template>> {
        return try {
            val response = templateService.getTemplates(type)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRouterTemplateConfig(routerId: String): Result<RouterTemplateConfig> {
        return try {
            val response = templateService.getRouterTemplateConfig(routerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRouterTemplateConfig(
        routerId: String,
        loginTemplateId: String? = null,
        paymentTemplateId: String? = null,
        mobileMoneyEnabled: Boolean? = null
    ): Result<RouterTemplateConfig> {
        return try {
            val request = UpdateRouterTemplateRequest(
                loginTemplateId = loginTemplateId,
                paymentTemplateId = paymentTemplateId,
                mobileMoneyEnabled = mobileMoneyEnabled
            )
            val response = templateService.updateRouterTemplateConfig(routerId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(file: File): Result<FileUploadResponse> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = templateService.uploadFile(body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
