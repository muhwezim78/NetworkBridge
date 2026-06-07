package com.muhwezi.networkbridge.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.vpnDataStore: DataStore<Preferences> by preferencesDataStore(name = "vpn_prefs")

@Singleton
class VpnPreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private val VPN_ENABLED_KEY = booleanPreferencesKey("vpn_enabled")
        private val LAST_PHONE_IP_KEY = stringPreferencesKey("last_phone_ip")
        private val LAST_ALLOWED_IPS_KEY = stringPreferencesKey("last_allowed_ips")
    }

    val vpnEnabled: Flow<Boolean> = context.vpnDataStore.data.map { preferences ->
        preferences[VPN_ENABLED_KEY] ?: false
    }

    val lastPhoneIp: Flow<String?> = context.vpnDataStore.data.map { preferences ->
        preferences[LAST_PHONE_IP_KEY]
    }

    val lastAllowedIps: Flow<String?> = context.vpnDataStore.data.map { preferences ->
        preferences[LAST_ALLOWED_IPS_KEY]
    }

    suspend fun setVpnEnabled(enabled: Boolean) {
        context.vpnDataStore.edit { preferences ->
            preferences[VPN_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveLastConnection(phoneIp: String, allowedIps: String) {
        context.vpnDataStore.edit { preferences ->
            preferences[LAST_PHONE_IP_KEY] = phoneIp
            preferences[LAST_ALLOWED_IPS_KEY] = allowedIps
        }
    }

    suspend fun clearVpnPrefs() {
        context.vpnDataStore.edit { preferences ->
            preferences.remove(VPN_ENABLED_KEY)
            preferences.remove(LAST_PHONE_IP_KEY)
            preferences.remove(LAST_ALLOWED_IPS_KEY)
        }
    }
}
