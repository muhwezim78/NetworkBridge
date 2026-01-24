package com.muhwezi.networkbridge.ui.router

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    onNavigateToPPPoEPlans: (String) -> Unit = {},
    onNavigateToTerminal: (String) -> Unit = {},
    onNavigateToPPPoEUsers: (String) -> Unit = {},
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
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
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
                                DetailItem(label = "Board Name", value = router.boardName)
                                DetailItem(label = "OS Version", value = router.osVersion)
                                DetailItem(label = "Uptime", value = router.uptime)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        DetailItem(label = "CPU Load", value = router.cpuLoad)
                                        DetailItem(label = "Active Users", value = router.activeUsers?.toString())
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        DetailItem(label = "Memory", value = router.memoryUsage)
                                        DetailItem(label = "Last Seen", value = router.lastSeen)
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                DetailItem(label = "Serial Number", value = router.serialNumber)
                                DetailItem(label = "DDNS Name", value = router.ddnsName)
                                
                                if (router.wgPublicKey != null) {
                                    DetailItem(label = "WireGuard Public Key", value = router.wgPublicKey)
                                }

                                if (router.wireguardConfig != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "WireGuard Setup Script",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text(
                                            text = router.wireguardConfig,
                                            modifier = Modifier.padding(8.dp),
                                            style = androidx.compose.ui.text.TextStyle(
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                fontSize = androidx.compose.ui.unit.TextUnit.Unspecified // use default
                                            )
                                        )
                                    }
                                }
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

                        Button(
                            onClick = { onNavigateToPPPoEPlans(router.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("PPPoE Plans")
                        }

                        Button(
                            onClick = { onNavigateToTerminal(router.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Router Terminal")
                        }

                        Button(
                            onClick = { onNavigateToPPPoEUsers(router.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("PPPoE Users")
                        }

                        // Setup Actions
                        Divider()
                        Text(
                            text = "Admin Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = viewModel::setupRemoteLogging,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Setup Logs", style = MaterialTheme.typography.labelSmall)
                            }
                            Button(
                                onClick = viewModel::setupTrafficFlow,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Setup Traffic", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // New Sections: Info/Logs/Backups/Pools
                        var selectedTab by remember { mutableStateOf(0) }
                        val tabs = listOf("Logs", "Backups", "IP Pools")
                        
                        ScrollableTabRow(selectedTabIndex = selectedTab) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { 
                                        selectedTab = index 
                                        when(index) {
                                            0 -> viewModel.loadLogs()
                                            1 -> viewModel.loadBackups()
                                            2 -> viewModel.loadIPPools()
                                        }
                                    },
                                    text = { Text(title) }
                                )
                            }
                        }

                        when (selectedTab) {
                            0 -> LogsList(uiState.logs, uiState.isLoadingLogs)
                            1 -> BackupsList(uiState.backups, uiState.isLoadingBackups, viewModel::createBackup)
                            2 -> PoolsList(uiState.pools, uiState.isLoadingPools)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogsList(logs: List<com.muhwezi.networkbridge.data.model.RouterLog>, isLoading: Boolean) {
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else if (logs.isEmpty()) {
        Text("No logs found.", modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            logs.forEach { log ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "${log.timestamp} [${log.severity}] ${log.topic}", style = MaterialTheme.typography.labelSmall)
                    Text(text = log.message ?: "", style = MaterialTheme.typography.bodySmall)
                    Divider(modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun BackupsList(
    backups: List<com.muhwezi.networkbridge.data.model.RouterBackup>, 
    isLoading: Boolean,
    onCreateBackup: () -> Unit
) {
    Column {
        Button(
            onClick = onCreateBackup,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Manual Backup")
        }
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
        } else if (backups.isEmpty()) {
            Text("No backups found.", modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
            backups.forEach { backup ->
                ListItem(
                    headlineContent = { Text(backup.filename ?: "Unknown") },
                    supportingContent = { Text("${backup.backupType ?: "file"} - ${backup.fileSize ?: 0} bytes") },
                    trailingContent = { Text(backup.createdAt?.substringBefore("T") ?: "") }
                )
                Divider()
            }
        }
        }
    }
}

@Composable
fun PoolsList(pools: List<com.muhwezi.networkbridge.data.model.IPPool>, isLoading: Boolean) {
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else if (pools.isEmpty()) {
        Text("No IP pools found.", modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            pools.forEach { pool ->
                ListItem(
                    headlineContent = { Text(pool.name ?: "Unnamed Pool") },
                    supportingContent = { Text(pool.ranges ?: "") },
                    trailingContent = { Text("${pool.usedCount}/${pool.totalCount}") }
                )
                Divider()
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String?, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(text = value ?: "N/A", style = MaterialTheme.typography.bodyLarge, color = color)
    }
}
