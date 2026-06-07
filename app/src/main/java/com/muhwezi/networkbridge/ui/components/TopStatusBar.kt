package com.muhwezi.networkbridge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhwezi.networkbridge.data.model.VpnState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopStatusBar(
    isLive: Boolean,
    routersOnline: Long,
    totalRouters: Long,
    todayIncome: Double,
    activeVouchers: Long,
    vpnState: VpnState,
    unreadNotifications: Int,
    onClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        while (true) {
            scrollState.animateScrollTo(
                value = scrollState.maxValue,
                animationSpec = tween(durationMillis = 20000, easing = LinearEasing)
            )
            scrollState.scrollTo(0)
        }
    }

    val vpnColor = when (vpnState) {
        is VpnState.Connected -> Color(0xFF4CAF50)
        is VpnState.Connecting -> Color(0xFFFFA500)
        is VpnState.Error -> MaterialTheme.colorScheme.error
        is VpnState.Idle -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(8.dp)
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Left: VPN button ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "VPN",
                        tint = vpnColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ── Centre: Scrolling Ticker ──────────────────────────────
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState, enabled = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                repeat(4) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        // Brand
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TerraConnect",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " Uganda",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Live Status dot
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLive) Color.Green.copy(alpha = alpha) else Color.Gray
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLive) "LIVE" else "OFFLINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = if (isLive) Color.Green else Color.Gray
                            )
                        }

                        // Metrics
                        StatusMetricItem(
                            icon = Icons.Default.CheckCircle,
                            value = "$routersOnline/$totalRouters",
                            label = "Online",
                            color = if (routersOnline > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                        StatusMetricItem(
                            icon = Icons.Default.ShoppingCart,
                            value = "UGX ${String.format("%,.0f", todayIncome)}",
                            label = "Income",
                            color = Color(0xFF4CAF50)
                        )
                        StatusMetricItem(
                            icon = Icons.Default.Info,
                            value = activeVouchers.toString(),
                            label = "Vouchers",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } // end ticker

            // ── Right: Notification bell ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onNotificationClick) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifications > 0) {
                                Badge { Text(unreadNotifications.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (unreadNotifications > 0)
                                Icons.Default.NotificationsActive
                            else
                                Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (unreadNotifications > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusMetricItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
