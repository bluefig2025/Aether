package com.aether.browser.core.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aether.browser.core.model.InterfaceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.aetherDataStore by preferencesDataStore(name = "aether_settings")

class SettingsRepository(private val context: Context) {
    private val interfaceModeKey = stringPreferencesKey("interface_mode")

    val interfaceMode: Flow<InterfaceMode> = context.aetherDataStore.data.map { prefs ->
        prefs[interfaceModeKey]
            ?.let { saved -> InterfaceMode.entries.firstOrNull { it.name == saved } }
            ?: InterfaceMode.AUTOMATIC
    }

    suspend fun setInterfaceMode(mode: InterfaceMode) {
        context.aetherDataStore.edit { it[interfaceModeKey] = mode.name }
    }
}
