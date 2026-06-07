package com.muhwezi.networkbridge.ui.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadSubscriptionStatus) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Error/Success Messages
                    uiState.error?.let {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    uiState.successMessage?.let {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Subscription Status
                    uiState.subscriptionStatus?.let { status ->
                        SubscriptionStatusCard(status)
                    }

                    // Redeem Token Section
                    RedeemTokenSection(
                        code = uiState.redeemCode,
                        onCodeChange = viewModel::onRedeemCodeChange,
                        onRedeem = viewModel::redeemToken,
                        enabled = !uiState.isLoading
                    )

                    // Purchase Subscription (Mobile Money)
                    PurchaseSection(
                        packageType = uiState.packageType,
                        phoneNumber = "",
                        packages = uiState.packages,
                        onPackageChange = viewModel::onPackageTypeChange,
                        onPhoneChange = { },
                        onPay = { viewModel.initiateSubscription(it) },
                        enabled = !uiState.isLoading
                    )

                    // Admin Token Generation
                    if (uiState.isAdmin) {
                        GenerateTokenSection(
                            packageType = uiState.packageType,
                            duration = uiState.duration,
                            generatedToken = uiState.generatedToken,
                            packages = uiState.packages,
                            onPackageTypeChange = viewModel::onPackageTypeChange,
                            onDurationChange = viewModel::onDurationChange,
                            onGenerate = viewModel::generateToken,
                            enabled = !uiState.isLoading
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionStatusCard(status: com.muhwezi.networkbridge.data.model.SubscriptionStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Subscription",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            StatusRow("Package", status.packageType)
            StatusRow("Status", if (status.isActive) "Active" else "Inactive")
            StatusRow("Expires", status.expiresAt)
            StatusRow("Router Limit", "${status.routersUsed} / ${status.routerLimit}")
            
            if (status.routersUsed >= status.routerLimit) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Router limit reached",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RedeemTokenSection(
    code: String,
    onCodeChange: (String) -> Unit,
    onRedeem: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Redeem Token",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                label = { Text("Token Code") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onRedeem,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled && code.isNotBlank()
            ) {
                Text("Redeem")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateTokenSection(
    packageType: String,
    duration: String,
    generatedToken: String?,
    packages: List<com.muhwezi.networkbridge.data.model.SubscriptionPackage>,
    onPackageTypeChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onGenerate: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Generate Token (Admin)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            var expandedPackage by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expandedPackage,
                onExpandedChange = { if (enabled) expandedPackage = !expandedPackage }
            ) {
                OutlinedTextField(
                    value = packageType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Package Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPackage) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = enabled
                )
                ExposedDropdownMenu(
                    expanded = expandedPackage,
                    onDismissRequest = { expandedPackage = false }
                ) {
                    packages.forEach { pkg ->
                        DropdownMenuItem(
                            text = { Text(pkg.name) },
                            onClick = {
                                onPackageTypeChange(pkg.id) // passing package id/name
                                expandedPackage = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = duration,
                onValueChange = onDurationChange,
                label = { Text("Duration (days)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled && packageType.isNotBlank() && duration.isNotBlank()
            ) {
                Text("Generate Token")
            }
            
            generatedToken?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Generated Token:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseSection(
    packageType: String,
    phoneNumber: String,
    packages: List<com.muhwezi.networkbridge.data.model.SubscriptionPackage>,
    onPackageChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPay: (String) -> Unit,
    enabled: Boolean
) {
    var localPhone by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(phoneNumber) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Purchase Subscription (Mobile Money)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            var expandedPackage by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expandedPackage,
                onExpandedChange = { if (enabled) expandedPackage = !expandedPackage }
            ) {
                OutlinedTextField(
                    value = packageType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Package") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPackage) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = enabled
                )
                ExposedDropdownMenu(
                    expanded = expandedPackage,
                    onDismissRequest = { expandedPackage = false }
                ) {
                    packages.forEach { pkg ->
                        DropdownMenuItem(
                            text = { Text(pkg.name) },
                            onClick = {
                                onPackageChange(pkg.id)
                                expandedPackage = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = localPhone,
                onValueChange = { 
                    localPhone = it
                    onPhoneChange(it)
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { onPay(localPhone) },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled && packageType.isNotBlank() && localPhone.isNotBlank()
            ) {
                Text("Pay with Mobile Money")
            }
        }
    }
}
