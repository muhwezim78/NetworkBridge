package com.muhwezi.networkbridge.ui.admin.firewall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.muhwezi.networkbridge.data.model.GlobalFirewallAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalFirewallScreen(
    onNavigateBack: () -> Unit,
    viewModel: GlobalFirewallViewModel = hiltViewModel()
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
                title = { Text("Global Firewall") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddDialog) {
                Icon(Icons.Default.Add, "Add Rule")
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

                    // Rules List
                    if (uiState.addresses.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No global firewall rules found.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.addresses) { rule ->
                                FirewallRuleCard(rule)
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddFirewallRuleDialog(
            action = uiState.action,
            chain = uiState.chain,
            srcAddress = uiState.srcAddress,
            dstAddress = uiState.dstAddress,
            protocol = uiState.protocol,
            dstPort = uiState.dstPort,
            comment = uiState.comment,
            onActionChange = viewModel::onActionChange,
            onChainChange = viewModel::onChainChange,
            onSrcAddressChange = viewModel::onSrcAddressChange,
            onDstAddressChange = viewModel::onDstAddressChange,
            onProtocolChange = viewModel::onProtocolChange,
            onDstPortChange = viewModel::onDstPortChange,
            onCommentChange = viewModel::onCommentChange,
            onDismiss = viewModel::hideAddDialog,
            onAdd = viewModel::addRule
        )
    }
}

@Composable
fun FirewallRuleCard(rule: GlobalFirewallAddress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Action chip
                val actionColor = if (rule.action == "drop")
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
                Text(
                    text = (rule.action ?: "unknown").uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = actionColor
                )
                Text(
                    text = "chain: ${rule.chain ?: "?"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (!rule.srcAddress.isNullOrBlank()) {
                Text(
                    text = "Src: ${rule.srcAddress}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (!rule.dstAddress.isNullOrBlank()) {
                Text(
                    text = "Dst: ${rule.dstAddress}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (!rule.protocol.isNullOrBlank()) {
                Text(
                    text = "Proto: ${rule.protocol}" + if (!rule.dstPort.isNullOrBlank()) " :${rule.dstPort}" else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (!rule.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rule.comment ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFirewallRuleDialog(
    action: String,
    chain: String,
    srcAddress: String,
    dstAddress: String,
    protocol: String,
    dstPort: String,
    comment: String,
    onActionChange: (String) -> Unit,
    onChainChange: (String) -> Unit,
    onSrcAddressChange: (String) -> Unit,
    onDstAddressChange: (String) -> Unit,
    onProtocolChange: (String) -> Unit,
    onDstPortChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Firewall Rule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Action selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Action:", modifier = Modifier.width(60.dp))
                    FilterChip(
                        selected = action == "drop",
                        onClick = { onActionChange("drop") },
                        label = { Text("Drop") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = action == "accept",
                        onClick = { onActionChange("accept") },
                        label = { Text("Accept") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = chain,
                    onValueChange = onChainChange,
                    label = { Text("Chain *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = srcAddress,
                    onValueChange = onSrcAddressChange,
                    label = { Text("Source Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dstAddress,
                    onValueChange = onDstAddressChange,
                    label = { Text("Destination Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = protocol,
                        onValueChange = onProtocolChange,
                        label = { Text("Protocol") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = dstPort,
                        onValueChange = onDstPortChange,
                        label = { Text("Dst Port") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    label = { Text("Comment") },
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
                    Button(onClick = onAdd) {
                        Text("Add Rule")
                    }
                }
            }
        }
    }
}
