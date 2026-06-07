package com.muhwezi.networkbridge.data.repository

import android.content.Context
import com.muhwezi.networkbridge.data.local.VpnPreferencesManager
import com.muhwezi.networkbridge.data.model.VpnProvisionRequest
import com.muhwezi.networkbridge.data.model.VpnState
import com.muhwezi.networkbridge.data.remote.VpnApiService
import com.wireguard.crypto.KeyPair
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface VpnRepository {
    val vpnState: StateFlow<VpnState>
    suspend fun connect()
    suspend fun disconnect()
}

@Singleton
class VpnRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vpnApiService: VpnApiService,
    private val vpnPrefs: VpnPreferencesManager
) : VpnRepository {

    private val _vpnState = MutableStateFlow<VpnState>(VpnState.Idle)
    override val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val backend = GoBackend(context)
    private var currentTunnel: Tunnel? = null
    private var currentKeyPair: KeyPair? = null

    override suspend fun connect() {
        try {
            _vpnState.value = VpnState.Connecting
            
            // 1. Generate new KeyPair
            val keyPair = KeyPair()
            currentKeyPair = keyPair

            // 2. Call backend to provision
            val response = vpnApiService.provision(
                VpnProvisionRequest(wgPublicKey = keyPair.publicKey.toBase64())
            )

            if (response.isSuccessful && response.body() != null) {
                val provisionData = response.body()!!
                
                // 3. Save to Datastore
                vpnPrefs.saveLastConnection(provisionData.phoneIp, provisionData.allowedIps)
                vpnPrefs.setVpnEnabled(true)

                // 4. Build WireGuard Config
                val config = Config.Builder()
                    .setInterface(
                        Interface.Builder()
                            .addAddress(com.wireguard.config.InetNetwork.parse(provisionData.phoneIp + "/32"))
                            .parsePrivateKey(keyPair.privateKey.toBase64())
                            .build()
                    )
                    .addPeer(
                        Peer.Builder()
                            .setEndpoint(com.wireguard.config.InetEndpoint.parse(provisionData.serverEndpoint))
                            .parsePublicKey(provisionData.serverPublicKey)
                            .setPersistentKeepalive(25)
                            .apply {
                                provisionData.allowedIps.split(",").forEach { ip ->
                                    addAllowedIp(com.wireguard.config.InetNetwork.parse(ip.trim()))
                                }
                            }
                            .build()
                    )
                    .build()

                // 5. Connect Tunnel
                val tunnel = object : Tunnel {
                    override fun getName() = "terraconnect"
                    override fun onStateChange(newState: Tunnel.State) {
                        // Handled internally by GoBackend
                    }
                }
                
                withContext(Dispatchers.IO) {
                    backend.setState(tunnel, Tunnel.State.UP, config)
                }
                currentTunnel = tunnel

                val routerCount = provisionData.allowedIps.split(",").size

                _vpnState.value = VpnState.Connected(provisionData.phoneIp, routerCount)

            } else {
                _vpnState.value = VpnState.Error("Failed to provision VPN: ${response.code()}")
            }

        } catch (e: Exception) {
            _vpnState.value = VpnState.Error("Connection error: ${e.message}")
        }
    }

    override suspend fun disconnect() {
        try {
            // 1. Stop local tunnel
            currentTunnel?.let {
                withContext(Dispatchers.IO) {
                    backend.setState(it, Tunnel.State.DOWN, null)
                }
            }
            currentTunnel = null
            currentKeyPair = null

            // 2. Tell backend to deprovision
            try {
                vpnApiService.deprovision()
            } catch (e: Exception) {
                // Ignore API failures on disconnect
            }

            vpnPrefs.setVpnEnabled(false)
            _vpnState.value = VpnState.Idle

        } catch (e: Exception) {
            _vpnState.value = VpnState.Error("Disconnect error: ${e.message}")
        }
    }
}
