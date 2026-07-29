package net.metalbrain.paysmart.core.features.theme.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AppThemePreferenceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    fun observeThemeMode(): Flow<AppThemeMode> {
        return dataStore.data.map { prefs ->
            AppThemeMode.fromStorage(prefs[THEME_MODE_KEY])
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.storageValue
        }
    }
}
