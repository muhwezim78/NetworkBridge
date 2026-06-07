package com.muhwezi.networkbridge.data.remote

import com.muhwezi.networkbridge.data.model.VpnProvisionRequest
import com.muhwezi.networkbridge.data.model.VpnProvisionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface VpnApiService {
    @POST("vpn/provision")
    suspend fun provision(@Body req: VpnProvisionRequest): Response<VpnProvisionResponse>

    @DELETE("vpn/deprovision")
    suspend fun deprovision(): Response<Unit>
}
