package com.muhwezi.networkbridge.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.muhwezi.networkbridge.ui.accounting.AccountingScreen
import com.muhwezi.networkbridge.ui.admin.firewall.GlobalFirewallScreen
import com.muhwezi.networkbridge.ui.admin.users.UserManagementScreen
import com.muhwezi.networkbridge.ui.billing.BillingScreen
import com.muhwezi.networkbridge.ui.dashboard.DashboardContent
import com.muhwezi.networkbridge.ui.mikrotik.plans.HotspotPlansScreen
import com.muhwezi.networkbridge.ui.mikrotik.plans.PPPoEPlansScreen
import com.muhwezi.networkbridge.ui.mikrotik.users.ActiveUsersScreen
import com.muhwezi.networkbridge.ui.mikrotik.users.PPPoEUsersScreen
import com.muhwezi.networkbridge.ui.mikrotik.vouchers.VoucherScreen
import com.muhwezi.networkbridge.ui.notification.SmsAnalyticsScreen
import com.muhwezi.networkbridge.ui.router.CreateRouterScreen
import com.muhwezi.networkbridge.ui.router.RouterDetailsScreen
import com.muhwezi.networkbridge.ui.router.TerminalScreen
import com.muhwezi.networkbridge.ui.subscription.SubscriptionScreen
import com.muhwezi.networkbridge.ui.support.SupportChatScreen
import com.muhwezi.networkbridge.ui.template.TemplateScreen
import com.muhwezi.networkbridge.ui.profile.ProfileScreen
import com.muhwezi.networkbridge.ui.dashboard.DashboardViewModel
import com.muhwezi.networkbridge.ui.components.TopStatusBar
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.muhwezi.networkbridge.ui.viewmodels.VpnViewModel
import com.muhwezi.networkbridge.ui.vpn.VpnControlSheet
import com.muhwezi.networkbridge.ui.notification.NotificationViewModel
import com.muhwezi.networkbridge.ui.notification.NotificationScreen

/** Routes where the bottom bar and top status bar should be visible */
private val mainRoutes = setOf("dashboard", "billing", "sms_analytics", "support")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    onLogout: () -> Unit,
    deepLinkRoute: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Navigate to the deep-link destination when a notification tap delivers one
    LaunchedEffect(deepLinkRoute) {
        if (deepLinkRoute != null) {
            navController.navigate(deepLinkRoute) {
                launchSingleTop = true
            }
            onDeepLinkConsumed()
        }
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isMainRoute = currentRoute in mainRoutes

    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val uiState by dashboardViewModel.uiState.collectAsState()

    val vpnViewModel: VpnViewModel = hiltViewModel()
    val vpnState by vpnViewModel.vpnState.collectAsState()
    var showVpnSheet by remember { mutableStateOf(false) }

    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val unreadNotifications by notificationViewModel.unreadCount.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val vpnPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            vpnViewModel.toggleVpn()
        }
    }

    if (showVpnSheet) {
        VpnControlSheet(
            vpnState = vpnState,
            onToggle = { 
                val intent = android.net.VpnService.prepare(context)
                if (intent != null) {
                    vpnPermissionLauncher.launch(intent)
                } else {
                    vpnViewModel.toggleVpn()
                }
            },
            onDismiss = { showVpnSheet = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isMainRoute,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    navController = navController,
                    onCloseDrawer = { scope.launch { drawerState.close() } },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isMainRoute) {
                    TopStatusBar(
                        isLive = uiState.isLive,
                        routersOnline = uiState.routersOnline,
                        totalRouters = uiState.totalRouters,
                        todayIncome = uiState.todayIncome,
                        activeVouchers = uiState.activeVouchers,
                        vpnState = vpnState,
                        unreadNotifications = unreadNotifications,
                        onClick = { showVpnSheet = true },
                        onNotificationClick = { navController.navigate("notifications") }
                    )
                }
            },
            bottomBar = {
                if (isMainRoute) {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        BottomNavItem.items.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (item is BottomNavItem.More) {
                                        scope.launch { drawerState.open() }
                                    } else {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.padding(innerPadding)
            ) {
                // ========================
                // Primary tab screens
                // ========================

                composable("dashboard") {
                    DashboardContent(
                        onNavigateToCreateRouter = { navController.navigate("create_router") },
                        onNavigateToRouterDetails = { routerId ->
                            navController.navigate("router_details/$routerId")
                        }
                    )
                }

                composable("billing") {
                    BillingScreen(
                        onNavigateBack = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable("sms_analytics") {
                    SmsAnalyticsScreen(
                        onNavigateBack = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable("support") {
                    SupportChatScreen(
                        onNavigateBack = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable("notifications") {
                    NotificationScreen(
                        onNavigateBack = { navController.popBackStack() },
                        viewModel = notificationViewModel
                    )
                }

                // ========================
                // Drawer / secondary screens
                // ========================

                composable("accounting") {
                    AccountingScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("user_management") {
                    UserManagementScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToGlobalFirewall = { navController.navigate("global_firewall") }
                    )
                }

                composable("global_firewall") {
                    GlobalFirewallScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("subscription") {
                    SubscriptionScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("templates") {
                    TemplateScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ========================
                // Router sub-screens
                // ========================

                composable("create_router") {
                    CreateRouterScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSubscription = { navController.navigate("subscription") }
                    )
                }

                composable(
                    "router_details/{routerId}",
                    arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                ) {
                    RouterDetailsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPlans = { routerId ->
                            navController.navigate("hotspot_plans/$routerId")
                        },
                        onNavigateToVouchers = { routerId ->
                            navController.navigate("vouchers/$routerId")
                        },
                        onNavigateToActiveUsers = { routerId ->
                            navController.navigate("active_users/$routerId")
                        },
                        onNavigateToPPPoEPlans = { routerId ->
                            navController.navigate("pppoe_plans/$routerId")
                        },
                        onNavigateToTerminal = { routerId ->
                            navController.navigate("terminal/$routerId")
                        },
                        onNavigateToPPPoEUsers = { routerId ->
                            navController.navigate("pppoe_users/$routerId")
                        }
                    )
                }

                composable(
                    "hotspot_plans/{routerId}",
                    arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                ) {
                    HotspotPlansScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "vouchers/{routerId}",
                    arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                ) {
                    VoucherScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "active_users/{routerId}",
                    arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                ) {
                    ActiveUsersScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "pppoe_plans/{routerId}",
                    arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                ) {
                    PPPoEPlansScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "terminal/{routerId}",
                    arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                ) {
                    TerminalScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "pppoe_users/{routerId}",
                    arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                ) {
                    PPPoEUsersScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// =====================================================
// Drawer Content
// =====================================================

@Composable
private fun DrawerContent(
    navController: NavHostController,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            text = "Network Bridge",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        DrawerItem(
            icon = Icons.Default.Person,
            label = "Profile & Settings",
            onClick = {
                onCloseDrawer()
                navController.navigate("profile")
            }
        )

        DrawerItem(
            icon = Icons.Default.AccountBox,
            label = "Accounting & Stats",
            onClick = {
                onCloseDrawer()
                navController.navigate("accounting")
            }
        )

        DrawerItem(
            icon = Icons.Default.Person,
            label = "User Management",
            onClick = {
                onCloseDrawer()
                navController.navigate("user_management")
            }
        )

        DrawerItem(
            icon = Icons.Default.Star,
            label = "Subscription",
            onClick = {
                onCloseDrawer()
                navController.navigate("subscription")
            }
        )

        DrawerItem(
            icon = Icons.Default.Edit,
            label = "Templates",
            onClick = {
                onCloseDrawer()
                navController.navigate("templates")
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        DrawerItem(
            icon = Icons.Default.ExitToApp,
            label = "Logout",
            onClick = onLogout
        )
    }
}

@Composable
private fun DrawerItem(
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
