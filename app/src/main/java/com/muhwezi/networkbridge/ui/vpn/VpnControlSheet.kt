package com.muhwezi.networkbridge.ui.vpn

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.muhwezi.networkbridge.data.model.VpnState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnControlSheet(
    vpnState: VpnState,
    onToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.VpnKey,
                contentDescription = "VPN",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Secure Backend Connection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Connect directly to your isolated backend network. This allows you to communicate with offline routers via their internal tunnel IP.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontWeight = FontWeight.SemiBold)
                        val statusText = when (vpnState) {
                            is VpnState.Connected -> "Connected"
                            is VpnState.Connecting -> "Connecting..."
                            is VpnState.Error -> "Error"
                            is VpnState.Idle -> "Disconnected"
                        }
                        val statusColor = when (vpnState) {
                            is VpnState.Connected -> Color(0xFF4CAF50)
                            is VpnState.Connecting -> Color(0xFFFFA500)
                            is VpnState.Error -> MaterialTheme.colorScheme.error
                            is VpnState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
                    }

                    if (vpnState is VpnState.Connected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tunnel IP", style = MaterialTheme.typography.bodySmall)
                            Text(vpnState.phoneIp, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Accessible Routers", style = MaterialTheme.typography.bodySmall)
                            Text("${vpnState.routerCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (vpnState is VpnState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(vpnState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = vpnState !is VpnState.Connecting
            ) {
                Text(
                    if (vpnState is VpnState.Connected) "Disconnect" else "Connect"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
