package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.AddGlobalFirewallRequest
import com.muhwezi.networkbridge.data.model.GlobalFirewallAddress
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FirewallService {
    @POST("firewall/global")
    suspend fun addGlobalFirewallAddress(@Body request: AddGlobalFirewallRequest): Response<Unit>

    @GET("firewall/global")
    suspend fun getGlobalFirewallAddresses(): Response<List<GlobalFirewallAddress>>
}
