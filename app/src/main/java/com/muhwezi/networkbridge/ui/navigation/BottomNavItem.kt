package com.muhwezi.networkbridge.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines the primary bottom navigation destinations.
 * The "More" tab opens the side drawer instead of navigating to a screen.
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Default.Home)
    object Billing : BottomNavItem("billing", "Billing", Icons.Default.ShoppingCart)
    object SmsAnalytics : BottomNavItem("sms_analytics", "SMS", Icons.Default.Notifications)
    object Support : BottomNavItem("support", "Support", Icons.Default.Email)
    object More : BottomNavItem("more", "More", Icons.Default.Menu)

    companion object {
        val items = listOf(Dashboard, Billing, SmsAnalytics, Support, More)
    }
}
