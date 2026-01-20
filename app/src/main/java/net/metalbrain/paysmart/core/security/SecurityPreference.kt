package net.metalbrain.paysmart.core.security


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.domain.model.SecuritySettings
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson

private val Context.securityDataStore by preferencesDataStore("security_prefs")

@Singleton
class SecurityPreference @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val gson = Gson()

    companion object {
        private val LAST_UNLOCK = longPreferencesKey("last_unlock")
        private val CLOUD_SETTINGS = stringPreferencesKey("cloud_security_settings")
        private val LOCK_AFTER_MINUTES = intPreferencesKey("lock_after_minutes")
        private val PASSCODE_ENABLED = booleanPreferencesKey("passcode_enabled")
    }

    /* ---------------- Cloud Security Cache ---------------- */
    suspend fun saveCloudSecuritySettings(settings: SecuritySettings?) {
        context.securityDataStore.edit { prefs ->
            prefs[CLOUD_SETTINGS] = gson.toJson(settings)
            prefs[PASSCODE_ENABLED] = settings?.passcodeEnabled ?: false
            prefs[LOCK_AFTER_MINUTES] = settings?.lockAfterMinutes ?: 5
        }
    }

    val cloudSecuritySettingsFlow: Flow<SecuritySettings?> =
        context.securityDataStore.data.map { prefs ->
            prefs[CLOUD_SETTINGS]?.let {
                gson.fromJson(it, SecuritySettings::class.java)
            }
        }

    /* ---------------- Lock State ---------------- */
    suspend fun updateLastUnlock() {
        context.securityDataStore.edit {
            it[LAST_UNLOCK] = System.currentTimeMillis()
        }
    }

    val lastUnlockFlow: Flow<Long> =
        context.securityDataStore.data.map { it[LAST_UNLOCK] ?: 0L }

    val lockAfterMinutesFlow: Flow<Int> =
        context.securityDataStore.data.map { it[LOCK_AFTER_MINUTES] ?: 5 }

    val passcodeEnabledFlow: Flow<Boolean> =
        context.securityDataStore.data.map { it[PASSCODE_ENABLED] ?: false }

    suspend fun clear() {
        context.securityDataStore.edit { it.clear() }
    }
}
