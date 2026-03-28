package com.muhwezi.networkbridge.ui.router

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRouterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    viewModel: CreateRouterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.isSubscriptionLimitReached) {
        if (uiState.isSubscriptionLimitReached) {
            onNavigateToSubscription()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Router") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 0 && uiState.currentStep < 3) {
                            viewModel.goBack()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Step indicator
            StepIndicator(currentStep = uiState.currentStep)

            // Step content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (uiState.currentStep) {
                    0 -> ConnectionStep(uiState, viewModel)
                    1 -> VerifyStep(uiState, viewModel)
                    2 -> RegisterStep(uiState, viewModel)
                    3 -> CompleteStep(uiState, onNavigateBack)
                }

                // Error display
                AnimatedVisibility(visible = uiState.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.error ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Step Indicator ───────────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: Int) {
    val steps = listOf("Connect", "Verify", "Register", "Done")

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Progress bar
        LinearProgressIndicator(
            progress = (currentStep + 1) / steps.size.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Step labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index <= currentStep)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline,
                    fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─── Step 0: Connection ───────────────────────────────────────

@Composable
private fun ConnectionStep(uiState: CreateRouterUiState, viewModel: CreateRouterViewModel) {
    StepHeader(
        icon = Icons.Default.Wifi,
        title = "Connect to MikroTik",
        subtitle = "Enter the router's local IP address and API credentials. Make sure your phone is connected to the same WiFi network."
    )

    OutlinedTextField(
        value = uiState.ipAddress,
        onValueChange = viewModel::onIpChange,
        label = { Text("Router IP Address") },
        placeholder = { Text("192.168.88.1") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
    )

    OutlinedTextField(
        value = uiState.apiPort,
        onValueChange = viewModel::onPortChange,
        label = { Text("WebFig Port (REST)") },
        placeholder = { Text(if (uiState.useSsl) "443" else "80") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            if (uiState.apiPort == "8728" || uiState.apiPort == "8729") {
                Text(
                    text = "Warning: ${uiState.apiPort} is likely the raw API port. Use the WebFig port (usually 80 or 443) for REST.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                 Text("Default: 443 (HTTPS) or 80 (HTTP)")
            }
        },
        isError = uiState.apiPort == "8728" || uiState.apiPort == "8729"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Use SSL (HTTPS)",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Recommended for security",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.material3.Switch(
            checked = uiState.useSsl,
            onCheckedChange = viewModel::onUseSslChange
        )
    }

    OutlinedTextField(
        value = uiState.username,
        onValueChange = viewModel::onUsernameChange,
        label = { Text("MikroTik Username") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = uiState.password,
        onValueChange = viewModel::onPasswordChange,
        label = { Text("MikroTik Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = uiState.name,
        onValueChange = viewModel::onNameChange,
        label = { Text("Router Name (optional)") },
        placeholder = { Text("Will auto-detect from router") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = viewModel::testConnection,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isLoading && uiState.ipAddress.isNotBlank() && uiState.username.isNotBlank()
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Connecting...")
        } else {
            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Connection")
        }
    }
}

// ─── Step 1: Verify Router Info ──────────────────────────────

@Composable
private fun VerifyStep(uiState: CreateRouterUiState, viewModel: CreateRouterViewModel) {
    val info = uiState.localRouterInfo

    StepHeader(
        icon = Icons.Default.Router,
        title = "Router Detected",
        subtitle = "We successfully connected to your MikroTik router. Review the details below."
    )

    if (info != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Identity
                InfoRow("Identity", info.identity.name)
                Divider(modifier = Modifier.padding(vertical = 6.dp))

                // System Resource
                info.resource.boardName?.let { InfoRow("Board", it) }
                info.resource.version?.let { InfoRow("RouterOS Version", it) }
                info.resource.architectureName?.let { InfoRow("Architecture", it) }
                info.resource.uptime?.let { InfoRow("Uptime", it) }
                info.resource.cpuLoad?.let { InfoRow("CPU Load", "$it%") }

                if (info.resource.totalMemory != null && info.resource.freeMemory != null) {
                    val totalMB = info.resource.totalMemory / (1024 * 1024)
                    val freeMB = info.resource.freeMemory / (1024 * 1024)
                    InfoRow("Memory", "${freeMB}MB free / ${totalMB}MB total")
                }

                // Routerboard
                if (info.routerboard != null) {
                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                    info.routerboard.serialNumber?.let { InfoRow("Serial Number", it) }
                    info.routerboard.model?.let { InfoRow("Model", it) }
                    info.routerboard.currentFirmware?.let { InfoRow("Firmware", it) }
                }

                // Cloud / DDNS
                info.cloudInfo?.let { cloud ->
                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                    InfoRow("Cloud DDNS", if (cloud.ddnsEnabled == "yes") "Enabled" else "Disabled")
                    cloud.dnsName?.takeIf { it.isNotBlank() }?.let { InfoRow("DNS Name", it) }
                    cloud.publicAddress?.takeIf { it.isNotBlank() }?.let { InfoRow("Public IP", it) }
                }
            }
        }

        // Editable name field
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Router Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = viewModel::goBack,
            modifier = Modifier.weight(1f)
        ) {
            Text("Back")
        }
        Button(
            onClick = viewModel::registerRouter,
            modifier = Modifier.weight(2f),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registering...")
            } else {
                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register & Setup WireGuard")
            }
        }
    }
}

// ─── Step 2: Registering / Configuring ───────────────────────

@Composable
private fun RegisterStep(uiState: CreateRouterUiState, viewModel: CreateRouterViewModel) {
    StepHeader(
        icon = Icons.Default.CloudDone,
        title = "Configuring Remote Access",
        subtitle = "Registering router with the server and configuring the WireGuard tunnel..."
    )

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Setting up WireGuard tunnel...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        // If not loading and setup script is available but not yet pushed
        val router = uiState.registeredRouter
        if (router != null && !uiState.setupComplete) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Router registered successfully!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Ready to configure WireGuard tunnel for remote access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = viewModel::pushWireGuardConfig,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Setup WireGuard Tunnel")
            }
        }
    }
}

// ─── Step 3: Complete ────────────────────────────────────────

@Composable
private fun CompleteStep(uiState: CreateRouterUiState, onNavigateBack: () -> Unit) {
    val isFullSuccess = uiState.setupMessage?.contains("successfully") == true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = if (isFullSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
            contentDescription = null,
            tint = if (isFullSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = if (isFullSuccess) "Setup Complete!" else "Router Registered",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = uiState.setupMessage ?: "Router has been added to your account.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Show WireGuard config if available
        val wgConfig = uiState.registeredRouter?.wireguardConfig
        if (!wgConfig.isNullOrBlank() && !isFullSuccess) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "WireGuard Setup Script (manual)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = wgConfig,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Dashboard")
        }
    }
}

// ─── Shared Components ───────────────────────────────────────

@Composable
private fun StepHeader(icon: ImageVector, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
