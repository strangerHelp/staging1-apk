package com.strangerhelp.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_settings")

class ChatSettingsManager(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("chat_theme")
        val BUBBLE_COLOR_KEY = intPreferencesKey("bubble_color")
    }

    val themeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: "system"
        }
        
    val bubbleColorFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[BUBBLE_COLOR_KEY] ?: 0xFF006494.toInt() // Default to a primary color
        }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun saveBubbleColor(color: Int) {
        context.dataStore.edit { preferences ->
            preferences[BUBBLE_COLOR_KEY] = color
        }
    }
}
