package net.metalbrain.paysmart.core.security


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

private val Context.securityDataStore by preferencesDataStore("security_prefs")

@Singleton
class SecurityPreference @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val store = context.securityDataStore

    /* ---------- Local Security State (SOURCE OF TRUTH) ---------- */
    companion object {

        private val LOCAL_STATE_INITIALIZED = booleanPreferencesKey("local_state_initialized")
        private val BIOMETRIC_REQUIRED = booleanPreferencesKey("biometric_required")

        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")

        private val PASSWORD_ENABLED = booleanPreferencesKey("password_enabled")

        private val PASSCODE_ENABLED = booleanPreferencesKey("passcode_enabled")

        private val HAS_VERIFIED_EMAIL = booleanPreferencesKey("has_verified_email")

        private val ALLOW_FEDERATED_LINKING = booleanPreferencesKey("allow_federated_linking")

        private val SESSION_LOCKED = booleanPreferencesKey("session_locked")
        private val KILLSWITCH_ACTIVE = booleanPreferencesKey("killswitch_active")
        private val LAST_SYNCED = longPreferencesKey("last_synced")
        private val LAST_UNLOCK = longPreferencesKey("last_unlock")
        private val LOCK_AFTER_MINUTES = intPreferencesKey("lock_after_minutes")
        private val CLOUD_SETTINGS = stringPreferencesKey("cloud_security_settings")
        private val LOCAL_STATE_JSON = stringPreferencesKey("local_security_state_json")

    }

    private fun mapFromPrefs(prefs: Preferences): LocalSecuritySettingsModel {
        return LocalSecuritySettingsModel(
            biometricsRequired = prefs[BIOMETRIC_REQUIRED] ?: true,
            biometricsEnabled = prefs[BIOMETRIC_ENABLED] ?: false,
            passcodeEnabled = prefs[PASSCODE_ENABLED] ?: false,
            passwordEnabled = prefs[PASSWORD_ENABLED] ?: false,
            hasVerifiedEmail = prefs[HAS_VERIFIED_EMAIL] ?: false,
            allowFederatedLinking = prefs[ALLOW_FEDERATED_LINKING] ?: false,
            sessionLocked = prefs[SESSION_LOCKED] ?: true,
            killSwitchActive = prefs[KILLSWITCH_ACTIVE] ?: false,
            lockAfterMinutes = prefs[LOCK_AFTER_MINUTES] ?: 5,
            lastSynced = prefs[LAST_SYNCED] ?: 0L
        )
    }

    private fun parseLocalStateJson(raw: String?): LocalSecuritySettingsModel? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(raw, LocalSecuritySettingsModel::class.java)
        }.getOrNull()
    }

    private fun resolveLocalState(prefs: Preferences): LocalSecuritySettingsModel {
        val fromJson = parseLocalStateJson(prefs[LOCAL_STATE_JSON]) ?: return mapFromPrefs(prefs)

        return fromJson.copy(
            // Keep dedicated primitive keys authoritative for backward compatibility.
            biometricsRequired = prefs[BIOMETRIC_REQUIRED] ?: fromJson.biometricsRequired,
            biometricsEnabled = prefs[BIOMETRIC_ENABLED] ?: fromJson.biometricsEnabled,
            passcodeEnabled = prefs[PASSCODE_ENABLED] ?: fromJson.passcodeEnabled,
            passwordEnabled = prefs[PASSWORD_ENABLED] ?: fromJson.passwordEnabled,
            hasVerifiedEmail = prefs[HAS_VERIFIED_EMAIL] ?: fromJson.hasVerifiedEmail,
            allowFederatedLinking = prefs[ALLOW_FEDERATED_LINKING] ?: fromJson.allowFederatedLinking,
            sessionLocked = prefs[SESSION_LOCKED] ?: fromJson.sessionLocked,
            killSwitchActive = prefs[KILLSWITCH_ACTIVE] ?: fromJson.killSwitchActive,
            lockAfterMinutes = prefs[LOCK_AFTER_MINUTES] ?: fromJson.lockAfterMinutes,
            lastSynced = prefs[LAST_SYNCED] ?: fromJson.lastSynced
        )
    }

    /* ---------------- LOCAL STATE ---------------- */

    suspend fun saveLocalSecurityState(state: LocalSecuritySettingsModel) {
        context.securityDataStore.edit { prefs ->
            prefs[LOCAL_STATE_INITIALIZED] = true
            prefs[BIOMETRIC_REQUIRED] = state.biometricsRequired
            prefs[BIOMETRIC_ENABLED] = state.biometricsEnabled
            prefs[PASSWORD_ENABLED] = state.passwordEnabled
            prefs[PASSCODE_ENABLED] = state.passcodeEnabled
            prefs[HAS_VERIFIED_EMAIL] = state.hasVerifiedEmail
            prefs[ALLOW_FEDERATED_LINKING] = state.allowFederatedLinking ?: false
            prefs[SESSION_LOCKED] = state.sessionLocked
            prefs[KILLSWITCH_ACTIVE] = state.killSwitchActive
            prefs[LOCK_AFTER_MINUTES] = state.lockAfterMinutes ?: 5
            prefs[LAST_SYNCED] = state.lastSynced
            prefs[LOCAL_STATE_JSON] = gson.toJson(state)
        }
    }

    suspend fun saveLastSyncedTimestamp() {
        context.securityDataStore.edit {
            it[LAST_SYNCED] = System.currentTimeMillis()
        }
    }


    suspend fun loadLocalSecurityState(): LocalSecuritySettingsModel {
        val prefs = context.securityDataStore.data.first()
        return resolveLocalState(prefs)
    }

    val localSecurityStateFlow: Flow<LocalSecuritySettingsModel> =
        store.data.map { prefs ->
            resolveLocalState(prefs)
        }

    /* ---------------- Cloud Security Cache ---------------- */
    suspend fun saveCloudSecuritySettings(settings: SecuritySettingsModel?) {
        context.securityDataStore.edit { prefs ->
            prefs[CLOUD_SETTINGS] = gson.toJson(settings)
            prefs[LOCK_AFTER_MINUTES] = settings?.lockAfterMinutes ?: 5
        }
    }

    suspend fun loadCloudSecuritySettings(): SecuritySettingsModel? {
        val prefs = context.securityDataStore.data.first()
        return prefs[CLOUD_SETTINGS]?.let {
            gson.fromJson(it, SecuritySettingsModel::class.java)
        }

    }

    val cloudSecuritySettingsFlow: Flow<SecuritySettingsModel?> =
        context.securityDataStore.data.map { prefs ->
            prefs[CLOUD_SETTINGS]?.let {
                gson.fromJson(it, SecuritySettingsModel::class.java)
            }
        }


    /* ---------------- SESSION ---------------- */
    suspend fun lockSession() {
        store.edit { it[SESSION_LOCKED] = true }
    }

    suspend fun unlockSession() {
        store.edit { it[SESSION_LOCKED] = false }
        updateLastUnlock()
    }

    suspend fun updateLastUnlock() {
        store.edit { it[LAST_UNLOCK] = System.currentTimeMillis() }
    }

    fun shouldAutoLock(): Boolean = runBlocking {
        val prefs = store.data.first()
        val lastUnlock = prefs[LAST_UNLOCK] ?: return@runBlocking true
        val timeoutMinutes = prefs[LOCK_AFTER_MINUTES] ?: 5
        val elapsed = System.currentTimeMillis() - lastUnlock
        elapsed > timeoutMinutes * 60_000
    }

    fun mergeServerWithLocal(
        server: SecuritySettingsModel?,
        local: LocalSecuritySettingsModel
    ): LocalSecuritySettingsModel {
        return local.copy(
            // Server overrides requirements (e.g. what's required)
            biometricsRequired = server?.biometricsRequired ?: local.biometricsRequired,
            // Mirror account-level enablement from server/Room to avoid stale local truth.
            passcodeEnabled = server?.passcodeEnabled ?: local.passcodeEnabled,
            passwordEnabled = server?.passwordEnabled ?: local.passwordEnabled,
            biometricsEnabled = server?.biometricsEnabled ?: local.biometricsEnabled,
            biometricsEnabledAt = server?.biometricsEnabledAt ?: local.biometricsEnabledAt,
            localPassCodeSetAt = server?.localPassCodeSetAt ?: local.localPassCodeSetAt,
            localPasswordSetAt = server?.localPasswordSetAt ?: local.localPasswordSetAt,
            killSwitchActive = server?.killswitch ?: local.killSwitchActive,
            
            allowFederatedLinking = server?.allowFederatedLinking ?: local.allowFederatedLinking,

            lockAfterMinutes = server?.lockAfterMinutes ?: local.lockAfterMinutes,
            onboardingRequired = server?.onboardingRequired ?: local.onboardingRequired,
            onboardingCompleted = server?.onboardingCompleted ?: local.onboardingCompleted,

            hasVerifiedEmail = server?.hasVerifiedEmail ?: local.hasVerifiedEmail,
            emailVerificationSentAt = server?.emailVerificationSentAt ?: local.emailVerificationSentAt,
            emailToVerify = server?.emailToVerify ?: local.emailToVerify,
            hasAddedHomeAddress = server?.hasAddedHomeAddress ?: local.hasAddedHomeAddress,
            hasVerifiedIdentity = server?.hasVerifiedIdentity ?: local.hasVerifiedIdentity,

            tosAcceptedAt = server?.tosAcceptedAt ?: local.tosAcceptedAt,
            kycStatus = server?.kycStatus ?: local.kycStatus,
            updatedAt = server?.updatedAt ?: local.updatedAt,
            lastSynced = System.currentTimeMillis()
        )
    }

    /* ---------------- HELPERS ---------------- */

    fun hasPasscode(): Boolean {
        // 🔐 Should be backed by native hash existence
        return true
    }

    fun hasBiometricAuth(): Boolean {
        // 🔐 Should be backed by Android Keystore / BiometricManager
        return true
    }

    suspend fun clearAll() {
        store.edit { it.clear() }
    }

    /* ---------------- Lock State ---------------- */

    val isInitializedFlow: Flow<Boolean> =
        store.data.map { it[LOCAL_STATE_INITIALIZED] ?: false }

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
