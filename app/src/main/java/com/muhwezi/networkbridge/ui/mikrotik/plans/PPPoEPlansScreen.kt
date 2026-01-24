package com.muhwezi.networkbridge.ui.mikrotik.plans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.muhwezi.networkbridge.data.model.PPPoEPlan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PPPoEPlansScreen(
    onNavigateBack: () -> Unit,
    viewModel: PPPoEPlansViewModel = hiltViewModel()
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
                title = { Text("PPPoE Plans") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::syncPlans) {
                        Icon(Icons.Default.Refresh, "Sync")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showCreateDialog) {
                Icon(Icons.Default.Add, "Create Plan")
            }
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
                Column(modifier = Modifier.fillMaxSize()) {
                    // Messages
                    uiState.error?.let {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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

                    // Plans List
                    if (uiState.plans.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No plans found. Create one or sync from router.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.plans) { plan ->
                                PPPoEPlanCard(plan)
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreatePPPoEPlanDialog(
            planName = uiState.planName,
            planPrice = uiState.planPrice,
            localAddress = uiState.localAddress,
            remoteAddress = uiState.remoteAddress,
            rateLimit = uiState.rateLimit,
            onPlanNameChange = viewModel::onPlanNameChange,
            onPlanPriceChange = viewModel::onPlanPriceChange,
            onLocalAddressChange = viewModel::onLocalAddressChange,
            onRemoteAddressChange = viewModel::onRemoteAddressChange,
            onRateLimitChange = viewModel::onRateLimitChange,
            onDismiss = viewModel::hideCreateDialog,
            onCreate = viewModel::createPlan
        )
    }
}

@Composable
fun PPPoEPlanCard(plan: PPPoEPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = plan.name ?: "Unnamed Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Price: UGX ${plan.price}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "Local Address: ${plan.localAddress}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Remote Address: ${plan.remoteAddress}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Rate Limit: ${plan.rateLimit}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CreatePPPoEPlanDialog(
    planName: String,
    planPrice: String,
    localAddress: String,
    remoteAddress: String,
    rateLimit: String,
    onPlanNameChange: (String) -> Unit,
    onPlanPriceChange: (String) -> Unit,
    onLocalAddressChange: (String) -> Unit,
    onRemoteAddressChange: (String) -> Unit,
    onRateLimitChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Create PPPoE Plan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = planName,
                    onValueChange = onPlanNameChange,
                    label = { Text("Plan Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = planPrice,
                    onValueChange = onPlanPriceChange,
                    label = { Text("Price *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = localAddress,
                    onValueChange = onLocalAddressChange,
                    label = { Text("Local Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = remoteAddress,
                    onValueChange = onRemoteAddressChange,
                    label = { Text("Remote Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rateLimit,
                    onValueChange = onRateLimitChange,
                    label = { Text("Rate Limit (e.g. 10M/10M)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onCreate) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
