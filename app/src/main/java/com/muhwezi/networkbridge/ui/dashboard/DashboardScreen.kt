package com.muhwezi.networkbridge.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.muhwezi.networkbridge.data.model.Router
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCreateRouter: () -> Unit,
    onNavigateToRouterDetails: (String) -> Unit,
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToUserManagement: () -> Unit = {},
    onNavigateToAccounting: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    onNavigateToSubscription = {
                        scope.launch { drawerState.close() }
                        onNavigateToSubscription()
                    },
                    onNavigateToUserManagement = {
                        scope.launch { drawerState.close() }
                        onNavigateToUserManagement()
                    },
                    onNavigateToAccounting = {
                        scope.launch { drawerState.close() }
                        onNavigateToAccounting()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Network Dashboard")
                            if (uiState.isLive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Green, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Live",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Green
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(onClick = onNavigateToCreateRouter) {
                        Icon(Icons.Default.Add, contentDescription = "Add Router")
                    }
                    FloatingActionButton(onClick = viewModel::loadDevices) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
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
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SummaryCard("Total Routers", uiState.totalRouters.toString(), Icons.Default.Settings)
                                SummaryCard("Online", uiState.routersOnline.toString(), Icons.Default.CheckCircle)
                                SummaryCard("Income", "UGX ${uiState.todayIncome}", Icons.Default.ShoppingCart)
                                SummaryCard("Vouchers", uiState.activeVouchers.toString(), Icons.Default.Info)
                            }
                        }

                        items(uiState.devices) { device ->
                            DeviceCard(
                                device = device,
                                onCommand = viewModel::sendCommand,
                                onClick = { onNavigateToRouterDetails(device.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationDrawerContent(
    onNavigateToSubscription: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToAccounting: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            text = "Network Bridge",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        DrawerMenuItem(
            icon = Icons.Default.Star,
            label = "Subscription",
            onClick = onNavigateToSubscription
        )

        DrawerMenuItem(
            icon = Icons.Default.Person,
            label = "User Management",
            onClick = onNavigateToUserManagement
        )

        DrawerMenuItem(
            icon = Icons.Default.AccountBox,
            label = "Accounting & Stats",
            onClick = onNavigateToAccounting
        )
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCard(
    device: Router,
    onCommand: (String, String) -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = device.status,
                    color = if (device.status == "online") Color.Green else Color.Red
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "IP: ${device.hostIp ?: device.realIp ?: "N/A"}")
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onCommand(device.id, "ping") }) {
                    Text("Ping")
                }
                Button(onClick = { onCommand(device.id, "restart") }) {
                    Text("Restart")
                }
            }
        }
    }
}
@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.width(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
