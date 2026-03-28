package com.muhwezi.networkbridge.ui.router

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.local.TokenManager
import com.muhwezi.networkbridge.data.model.CreateRouterRequest
import com.muhwezi.networkbridge.data.model.LocalConnectionParams
import com.muhwezi.networkbridge.data.model.LocalRouterInfo
import com.muhwezi.networkbridge.data.model.Router
import com.muhwezi.networkbridge.data.repository.MikrotikLocalRepository
import com.muhwezi.networkbridge.data.repository.RouterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/**
 * Setup wizard steps:
 * 0 = Connect: Enter LAN IP, port, MikroTik credentials → test connection
 * 1 = Verify: Shows auto-discovered router info (name, model, serial, OS version)
 * 2 = Register: Sends to cloud backend → gets WireGuard config back
 * 3 = Complete: Auto-pushed WireGuard script to router → shows success
 */
@HiltViewModel
class CreateRouterViewModel @Inject constructor(
    private val routerRepository: RouterRepository,
    private val localRepository: MikrotikLocalRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRouterUiState())
    val uiState: StateFlow<CreateRouterUiState> = _uiState.asStateFlow()

    // --- Input field handlers ---

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onIpChange(ip: String) {
        _uiState.value = _uiState.value.copy(ipAddress = ip)
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onPortChange(port: String) {
        _uiState.value = _uiState.value.copy(apiPort = port)
    }

    fun onUseSslChange(useSsl: Boolean) {
        val currentState = _uiState.value
        // Auto-switch port if user hasn't typed a custom one (check if current port matches default for old SSL state)
        val oldDefault = if (currentState.useSsl) "443" else "80"
        val newDefault = if (useSsl) "443" else "80"
        
        val newPort = if (currentState.apiPort == oldDefault) newDefault else currentState.apiPort

        _uiState.value = currentState.copy(
            useSsl = useSsl,
            apiPort = newPort
        )
    }

    // --- Step navigation ---

    fun goBack() {
        val current = _uiState.value.currentStep
        if (current > 0) {
            _uiState.value = _uiState.value.copy(currentStep = current - 1, error = null)
        }
    }

    // --- Step 0 → 1: Test local connection ---

    fun testConnection() {
        if (_uiState.value.isLoading) return

        val state = _uiState.value
        if (state.ipAddress.isBlank()) {
            _uiState.value = state.copy(error = "Router IP address is required")
            return
        }
        if (state.username.isBlank()) {
            _uiState.value = state.copy(error = "Username is required")
            return
        }
        if (state.password.isBlank()) {
            _uiState.value = state.copy(error = "Password is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val params = buildConnectionParams()

            // First: verify credentials via identity endpoint
            val identityResult = localRepository.verifyConnection(params)
            if (identityResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = identityResult.exceptionOrNull()?.message ?: "Connection failed"
                )
                return@launch
            }

            // Then: fetch full router info
            val infoResult = localRepository.fetchRouterInfo(params)
            if (infoResult.isSuccess) {
                val info = infoResult.getOrNull()!!
                // Auto-populate name from router identity if empty
                val autoName = if (_uiState.value.name.isBlank()) {
                    info.identity.name
                } else {
                    _uiState.value.name
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    localRouterInfo = info,
                    name = autoName,
                    currentStep = 1
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = infoResult.exceptionOrNull()?.message ?: "Failed to fetch router details"
                )
            }
        }
    }

    // --- Step 1 → 2: Register with cloud backend ---

    fun registerRouter() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = tokenManager.userId.first()
                ?: tokenManager.accessToken.first()?.let { extractUserIdFromJwt(it) }
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not found. Please login again."
                )
                return@launch
            }

            val state = _uiState.value
            val info = state.localRouterInfo

            val request = CreateRouterRequest(
                userId = userId,
                name = state.name.ifBlank { info?.identity?.name ?: "Router" },
                username = state.username,
                password = state.password,
                realIp = state.ipAddress.ifBlank { null },
                serialNumber = info?.routerboard?.serialNumber,
                ddnsName = info?.cloudInfo?.dnsName?.takeIf { it.isNotBlank() }
            )

            val result = routerRepository.createRouter(request)
            if (result.isSuccess) {
                val router = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    registeredRouter = router,
                    currentStep = 2
                )
                // Auto-push WireGuard config if setup script is available
                if (!router.setupScript.isNullOrBlank()) {
                    pushWireGuardConfig()
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to register router"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMsg,
                    isSubscriptionLimitReached = errorMsg.contains("Subscription limit reached", true)
                )
            }
        }
    }

    // --- Step 2 → 3: Push WireGuard setup script to router ---

    fun pushWireGuardConfig() {
        val router = _uiState.value.registeredRouter ?: return
        val script = router.setupScript
        if (script.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                setupComplete = true,
                setupMessage = "Router registered successfully. No WireGuard setup script was provided by the server — you may need to configure WireGuard manually."
            )
            return
        }

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val params = buildConnectionParams()
            // Wrap script in cleanup logic to make it idempotent
            val safeScript = makeScriptIdempotent(script)
            val result = localRepository.pushSetupScript(params, safeScript)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    setupComplete = true,
                    currentStep = 3,
                    setupMessage = "WireGuard tunnel configured successfully! The router should appear online shortly."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    setupComplete = true,
                    currentStep = 3,
                    setupMessage = "Router registered, but WireGuard auto-setup failed: ${result.exceptionOrNull()?.message}. You can configure WireGuard manually using the script shown in router details."
                )
            }
        }
    }

    // --- Helper ---

    private fun buildConnectionParams(): LocalConnectionParams {
        val state = _uiState.value
        val port = state.apiPort.toIntOrNull() ?: 443
        return LocalConnectionParams(
            host = state.ipAddress,
            port = port,
            username = state.username,
            password = state.password,
            useSsl = state.useSsl
        )
    }

    /**
     * Prepends cleanup commands to the script to prevent "already exists" errors.
     * It attempts to remove the interface, IP address, and firewall rules created by previous runs.
     */
    private fun makeScriptIdempotent(script: String): String {
        // Extract interface name if present (usually wg-terraconnect)
        val nameMatch = Regex("name=([a-zA-Z0-9-]+)").find(script)
        val interfaceName = nameMatch?.groupValues?.get(1) ?: "wg-terraconnect"

        // Extract listen port if present (usually 13231)
        val portMatch = Regex("listen-port=([0-9]+)").find(script)
        val port = portMatch?.groupValues?.get(1)

        val builder = StringBuilder()
        builder.append("# Cleanup previous configuration to avoid collisions\n")
        
        // Remove IP addresses attached to this interface (referenced by interface name)
        builder.append(":do { /ip address remove [find interface=$interfaceName] } on-error={}\n")
        
        // Remove the WireGuard interface itself
        builder.append(":do { /interface wireguard remove [find name=$interfaceName] } on-error={}\n")

        // Remove firewall rules specific to this interface (safer than generic comments)
        // 1. Remove rules where in-interface is our interface
        builder.append(":do { /ip firewall filter remove [find in-interface=$interfaceName] } on-error={}\n")
        // 2. Remove rules where out-interface is our interface
        builder.append(":do { /ip firewall filter remove [find out-interface=$interfaceName] } on-error={}\n")
        
        // 3. Remove rules for the listen port (Input chain)
        if (port != null) {
             builder.append(":do { /ip firewall filter remove [find dst-port=$port protocol=udp chain=input] } on-error={}\n")
        }

        // Remove NAT rules specific to this interface
        builder.append(":do { /ip firewall nat remove [find out-interface=$interfaceName] } on-error={}\n")

        // Remove routes
        builder.append(":do { /ip route remove [find gateway=$interfaceName] } on-error={}\n")

        builder.append("\n# Original Script\n")
        builder.append(script)
        
        return builder.toString()
    }

    /**
     * Extract user ID from JWT token's "sub" claim by base64-decoding the payload.
     */
    private fun extractUserIdFromJwt(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            JSONObject(payload).optString("sub", null)
        } catch (e: Exception) {
            null
        }
    }
}

data class CreateRouterUiState(
    // Input fields
    val name: String = "",
    val ipAddress: String = "",
    val username: String = "admin",
    val password: String = "",

    val apiPort: String = "443",
    val useSsl: Boolean = true,
    // Wizard state
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Step 1 result: auto-discovered router info
    val localRouterInfo: LocalRouterInfo? = null,
    // Step 2 result: registered router from backend
    val registeredRouter: Router? = null,
    // Step 3 result: WireGuard setup outcome
    val setupComplete: Boolean = false,
    val setupMessage: String? = null,
    val isSubscriptionLimitReached: Boolean = false,
    val isSuccess: Boolean = false // kept for backward compat with navigation
)
