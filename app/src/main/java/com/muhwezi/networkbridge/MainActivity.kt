package com.muhwezi.networkbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.muhwezi.networkbridge.ui.theme.NetworkCoordinatorTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.muhwezi.networkbridge.ui.accounting.AccountingScreen
import com.muhwezi.networkbridge.ui.admin.users.UserManagementScreen
import com.muhwezi.networkbridge.ui.auth.LoginScreen
import com.muhwezi.networkbridge.ui.dashboard.DashboardScreen
import com.muhwezi.networkbridge.ui.mikrotik.plans.HotspotPlansScreen
import com.muhwezi.networkbridge.ui.mikrotik.users.ActiveUsersScreen
import com.muhwezi.networkbridge.ui.mikrotik.vouchers.VoucherScreen
import com.muhwezi.networkbridge.ui.router.CreateRouterScreen
import com.muhwezi.networkbridge.ui.router.RouterDetailsScreen
import com.muhwezi.networkbridge.ui.subscription.SubscriptionScreen
import com.muhwezi.networkbridge.ui.mikrotik.plans.PPPoEPlansScreen
import com.muhwezi.networkbridge.ui.admin.firewall.GlobalFirewallScreen
import com.muhwezi.networkbridge.ui.router.TerminalScreen
import com.muhwezi.networkbridge.ui.mikrotik.users.PPPoEUsersScreen
import com.muhwezi.networkbridge.data.local.TokenManager
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if token exists synchronously to determine start destination
        val tokenManager = TokenManager(applicationContext)
        val hasToken = runBlocking { tokenManager.token.first() } != null
        val startDest = if (hasToken) "dashboard" else "login"
        
        setContent {
            NetworkCoordinatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = startDest) {
                        // Authentication - Login
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToSignup = {
                                    navController.navigate("signup")
                                }
                            )
                        }

                        // Authentication - Signup
                        composable("signup") {
                            com.muhwezi.networkbridge.ui.auth.SignupScreen(
                                onSignupSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Dashboard
                        composable("dashboard") {
                            DashboardScreen(
                                onNavigateToCreateRouter = { navController.navigate("create_router") },
                                onNavigateToRouterDetails = { routerId -> 
                                    navController.navigate("router_details/$routerId") 
                                },
                                onNavigateToSubscription = { navController.navigate("subscription") },
                                onNavigateToUserManagement = { navController.navigate("user_management") },
                                onNavigateToAccounting = { navController.navigate("accounting") }
                            )
                        }

                        // Router Management
                        composable("create_router") {
                            CreateRouterScreen(
                                onNavigateBack = { navController.popBackStack() }
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

                        // Subscription Management
                        composable("subscription") {
                            SubscriptionScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Mikrotik Features - Hotspot Plans
                        composable(
                            "hotspot_plans/{routerId}",
                            arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                        ) {
                            HotspotPlansScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Mikrotik Features - Vouchers
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

                        // Mikrotik Features - PPPoE Plans
                        composable(
                            "pppoe_plans/{routerId}",
                            arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                        ) {
                            PPPoEPlansScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // User Management (Admin)
                        composable("user_management") {
                            UserManagementScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToGlobalFirewall = { navController.navigate("global_firewall") }
                            )
                        }

                        // Global Firewall (Admin)
                        composable("global_firewall") {
                            GlobalFirewallScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Accounting & Dashboard
                        composable("accounting") {
                            AccountingScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Router Terminal
                        composable(
                            "terminal/{routerId}",
                            arguments = listOf(navArgument("routerId") { type = NavType.StringType })
                        ) {
                            TerminalScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // PPPoE Users
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
    }
}
