package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.*
import com.muhwezi.networkbridge.data.remote.MikrotikService
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MikrotikRepository @Inject constructor(
    private val mikrotikService: MikrotikService
) {
    suspend fun createPPPoEUser(request: PPPoEUserRequest): Result<Unit> {
        return try {
            val response = mikrotikService.createPPPoEUser(request)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createHotspotPlan(request: CreateHotspotPlanRequest): Result<String> {
        return try {
            val response = mikrotikService.createHotspotPlan(request)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getHotspotPlans(routerId: String): Result<List<HotspotPlan>> {
        return try {
            val response = mikrotikService.getHotspotPlans(routerId)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun syncHotspotPlans(routerId: String): Result<Unit> {
        return try {
            val response = mikrotikService.syncHotspotPlans(routerId)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun generateVouchers(request: GenerateVouchersRequest): Result<GenerateVouchersResponse> {
        return try {
            val response = mikrotikService.generateVouchers(request)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getVouchers(routerId: String?): Result<List<VoucherResponse>> {
        return try {
            val response = mikrotikService.getVouchers(routerId)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun generateVoucherPdf(request: VoucherPdfRequest): Result<ResponseBody> {
        return try {
            val response = mikrotikService.generateVoucherPdf(request)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getActiveHotspotUsers(routerId: String): Result<List<ActiveHotspotUser>> {
        return try {
            val response = mikrotikService.getActiveHotspotUsers(routerId)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getHotspotUsers(routerId: String): Result<List<HotspotUserResponse>> {
        return try {
            val response = mikrotikService.getHotspotUsers(routerId)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getHotspotProfiles(routerId: String): Result<List<HotspotProfile>> {
        return try {
            val response = mikrotikService.getHotspotProfiles(routerId)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun executeCommand(routerId: String, command: String): Result<ExecuteCommandResponse> {
        return try {
            val response = mikrotikService.executeCommand(routerId, ExecuteCommandRequest(command))
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
