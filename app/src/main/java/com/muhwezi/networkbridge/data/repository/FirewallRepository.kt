package com.muhwezi.networkbridge.data.repository

import com.muhwezi.networkbridge.data.model.AddGlobalFirewallRequest
import com.muhwezi.networkbridge.data.model.GlobalFirewallAddress
import com.muhwezi.networkbridge.data.remote.FirewallService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirewallRepository @Inject constructor(
    private val firewallService: FirewallService
) {
    suspend fun addGlobalFirewallAddress(request: AddGlobalFirewallRequest): Result<Unit> {
        return try {
            val response = firewallService.addGlobalFirewallAddress(request)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getGlobalFirewallAddresses(): Result<List<GlobalFirewallAddress>> {
        return try {
            val response = firewallService.getGlobalFirewallAddresses()
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
