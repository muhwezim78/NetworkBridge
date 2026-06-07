package com.muhwezi.networkbridge

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.muhwezi.networkbridge.ui.theme.NetworkCoordinatorTheme
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muhwezi.networkbridge.ui.auth.LoginScreen
import com.muhwezi.networkbridge.ui.navigation.MainScaffold
import com.muhwezi.networkbridge.data.local.TokenManager
import com.muhwezi.networkbridge.data.local.SessionEvent
import com.muhwezi.networkbridge.data.local.SessionManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.messaging.FirebaseMessaging
import com.muhwezi.networkbridge.data.remote.DeviceService
import com.muhwezi.networkbridge.data.remote.FcmTokenRequest
import com.muhwezi.networkbridge.service.AppFirebaseMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var themeManager: com.muhwezi.networkbridge.data.local.ThemeManager
    @Inject lateinit var deviceService: DeviceService

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                Log.d("FCM", "Notification permission granted")
            } else {
                Log.d("FCM", "Notification permission denied")
            }
        }

    // Holds the pending deep-link route from notification taps.
    // Fed by both onCreate (cold start) and onNewIntent (warm start).
    private val pendingDeepLink = mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val target = intent.getStringExtra(AppFirebaseMessagingService.EXTRA_NAVIGATE_TO)
        if (target != null) {
            pendingDeepLink.value = target
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Check if token exists synchronously to determine start destination
        val tokenManager = TokenManager(applicationContext)
        val hasToken = runBlocking { tokenManager.accessToken.first() } != null
        val startDest = if (hasToken) "main" else "login"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "Fetching token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                // Register with backend (fire-and-forget)
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching { deviceService.registerFcmToken(FcmTokenRequest(token)) }
                        .onFailure { Log.w("FCM", "Token registration failed: ${it.message}") }
                }
            }
        
        setContent {
            val themeMode by themeManager.themeMode.collectAsState(initial = com.muhwezi.networkbridge.data.local.ThemeMode.SYSTEM)
            
            NetworkCoordinatorTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Observe session expiry events globally — redirect to login
                    LaunchedEffect(Unit) {
                        sessionManager.sessionEvents.collect { event ->
                            when (event) {
                                is SessionEvent.Expired -> {
                                    navController.navigate("login") {
                                        // Clear entire back stack so all ViewModels
                                        // with stale data are torn down
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        }
                    }

                    // Seed the deep-link from the launch intent on cold start
                    LaunchedEffect(Unit) {
                        val target = intent?.getStringExtra(
                            AppFirebaseMessagingService.EXTRA_NAVIGATE_TO
                        )
                        if (target != null) {
                            pendingDeepLink.value = target
                        }
                    }

                    NavHost(navController = navController, startDestination = startDest) {
                        // Authentication - Login
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("main") {
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
                                    navController.navigate("main") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Main app shell — contains bottom nav, drawer, and all authenticated screens
                        composable("main") {
                            val deepLink by pendingDeepLink

                            MainScaffold(
                                onLogout = {
                                    // sessionManager.onSessionExpired() fires the
                                    // SessionEvent.Expired which the LaunchedEffect above
                                    // catches and navigates to login with full stack clear.
                                    sessionManager.onSessionExpired()
                                },
                                deepLinkRoute = deepLink,
                                onDeepLinkConsumed = { pendingDeepLink.value = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

