package com.example.readingcorner.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/**
 * Lightweight wrapper over Jetpack DataStore (satisfies the SharedPreferences/DataStore
 * requirement). Stores the last search query and a dark-theme toggle.
 */
class PreferencesManager(private val context: Context) {

    private object Keys {
        val LAST_QUERY = stringPreferencesKey("last_search_query")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val lastSearchQuery: Flow<String> =
        context.dataStore.data.map { it[Keys.LAST_QUERY] ?: "" }

    val darkTheme: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.DARK_THEME] ?: false }

    suspend fun setLastSearchQuery(query: String) {
        context.dataStore.edit { it[Keys.LAST_QUERY] = query }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }
}
