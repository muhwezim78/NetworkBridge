package com.muhwezi.networkbridge.ui.router

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouterDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlans: (String) -> Unit = {},
    onNavigateToVouchers: (String) -> Unit = {},
    onNavigateToActiveUsers: (String) -> Unit = {},
    viewModel: RouterDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.router?.name ?: "Router Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteRouter(onNavigateBack) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                uiState.router?.let { router ->
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Router Details Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                DetailItem(label = "Name", value = router.name)
                                DetailItem(label = "IP Address", value = router.hostIp ?: router.realIp ?: "N/A")
                                DetailItem(
                                    label = "Status",
                                    value = router.status,
                                    color = if (router.status == "online") Color.Green else Color.Red
                                )
                            }
                        }

                        // Mikrotik Features Section
                        Text(
                            text = "Mikrotik Features",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        // Feature Buttons
                        Button(
                            onClick = { onNavigateToPlans(router.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Hotspot Plans")
                        }

                        Button(
                            onClick = { onNavigateToVouchers(router.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Vouchers")
                        }

                        Button(
                            onClick = { onNavigateToActiveUsers(router.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Active Users")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = color)
    }
}
