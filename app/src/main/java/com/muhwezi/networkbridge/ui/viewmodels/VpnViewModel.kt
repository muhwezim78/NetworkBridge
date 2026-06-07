package com.muhwezi.networkbridge.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.VpnState
import com.muhwezi.networkbridge.data.repository.VpnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnViewModel @Inject constructor(
    private val vpnRepository: VpnRepository
) : ViewModel() {

    val vpnState: StateFlow<VpnState> = vpnRepository.vpnState

    fun toggleVpn() {
        viewModelScope.launch {
            if (vpnState.value is VpnState.Connected || vpnState.value is VpnState.Connecting) {
                vpnRepository.disconnect()
            } else {
                vpnRepository.connect()
            }
        }
    }

    fun disconnectVpn() {
        viewModelScope.launch {
            vpnRepository.disconnect()
        }
    }
}
