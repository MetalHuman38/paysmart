package net.metalbrain.paysmart.core.features.account.security.data


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.Timestamp
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
        private val PASSKEY_ENABLED = booleanPreferencesKey("passkey_enabled")

        private val RECOVERY_METHOD_READY = booleanPreferencesKey("recovery_method_ready")
        private val HAS_VERIFIED_EMAIL = booleanPreferencesKey("has_verified_email")
        private val HAS_ADDED_HOME_ADDRESS = booleanPreferencesKey("has_added_home_address")
        private val HAS_VERIFIED_IDENTITY = booleanPreferencesKey("has_verified_identity")
        private val HAS_ENROLLED_MFA_FACTOR = booleanPreferencesKey("has_enrolled_mfa_factor")
        private val HAS_SKIPPED_MFA_ENROLLMENT_PROMPT =
            booleanPreferencesKey("has_skipped_mfa_enrollment_prompt")
        private val HAS_SKIPPED_PASSKEY_ENROLLMENT_PROMPT =
            booleanPreferencesKey("has_skipped_passkey_enrollment_prompt")
        private val HIDE_BALANCE = booleanPreferencesKey("hide_balance")
        private val PRIVACY_CREDIT_ENABLED = booleanPreferencesKey("privacy_credit_enabled")
        private val PRIVACY_SOCIAL_MEDIA_ENABLED = booleanPreferencesKey("privacy_social_media_enabled")

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
            passkeyEnabled = prefs[PASSKEY_ENABLED] ?: false,
            recoveryMethodReady = prefs[RECOVERY_METHOD_READY] ?: false,
            hasVerifiedEmail = prefs[HAS_VERIFIED_EMAIL] ?: false,
            hasAddedHomeAddress = prefs[HAS_ADDED_HOME_ADDRESS],
            hasVerifiedIdentity = prefs[HAS_VERIFIED_IDENTITY] ?: false,
            hasEnrolledMfaFactor = prefs[HAS_ENROLLED_MFA_FACTOR] ?: false,
            hasSkippedMfaEnrollmentPrompt = prefs[HAS_SKIPPED_MFA_ENROLLMENT_PROMPT] ?: false,
            hasSkippedPasskeyEnrollmentPrompt = prefs[HAS_SKIPPED_PASSKEY_ENROLLMENT_PROMPT] ?: true,
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
            passkeyEnabled = prefs[PASSKEY_ENABLED] ?: fromJson.passkeyEnabled,
            recoveryMethodReady = prefs[RECOVERY_METHOD_READY] ?: fromJson.recoveryMethodReady,
            hasVerifiedEmail = prefs[HAS_VERIFIED_EMAIL] ?: fromJson.hasVerifiedEmail,
            hasAddedHomeAddress = prefs[HAS_ADDED_HOME_ADDRESS] ?: fromJson.hasAddedHomeAddress,
            hasVerifiedIdentity = prefs[HAS_VERIFIED_IDENTITY] ?: fromJson.hasVerifiedIdentity,
            hasEnrolledMfaFactor = prefs[HAS_ENROLLED_MFA_FACTOR] ?: fromJson.hasEnrolledMfaFactor,
            hasSkippedMfaEnrollmentPrompt = prefs[HAS_SKIPPED_MFA_ENROLLMENT_PROMPT]
                ?: fromJson.hasSkippedMfaEnrollmentPrompt,
            hasSkippedPasskeyEnrollmentPrompt = prefs[HAS_SKIPPED_PASSKEY_ENROLLMENT_PROMPT]
                ?: fromJson.hasSkippedPasskeyEnrollmentPrompt,
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
            prefs[PASSKEY_ENABLED] = state.passkeyEnabled
            prefs[RECOVERY_METHOD_READY] = state.recoveryMethodReady
            prefs[HAS_VERIFIED_EMAIL] = state.hasVerifiedEmail
            prefs[HAS_ADDED_HOME_ADDRESS] = state.hasAddedHomeAddress == true
            prefs[HAS_VERIFIED_IDENTITY] = state.hasVerifiedIdentity
            prefs[HAS_ENROLLED_MFA_FACTOR] = state.hasEnrolledMfaFactor
            prefs[HAS_SKIPPED_MFA_ENROLLMENT_PROMPT] = state.hasSkippedMfaEnrollmentPrompt
            prefs[HAS_SKIPPED_PASSKEY_ENROLLMENT_PROMPT] = state.hasSkippedPasskeyEnrollmentPrompt
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

    suspend fun markRecoveryMethodReady() {
        context.securityDataStore.edit { prefs ->
            prefs[RECOVERY_METHOD_READY] = true
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

    val hideBalanceFlow: Flow<Boolean> =
        store.data.map { prefs ->
            prefs[HIDE_BALANCE] ?: false
        }

    val privacyCreditEnabledFlow: Flow<Boolean> =
        store.data.map { prefs ->
            prefs[PRIVACY_CREDIT_ENABLED] ?: false
        }

    val privacySocialMediaEnabledFlow: Flow<Boolean> =
        store.data.map { prefs ->
            prefs[PRIVACY_SOCIAL_MEDIA_ENABLED] ?: false
        }

    suspend fun setHideBalance(hidden: Boolean) {
        store.edit { prefs ->
            prefs[HIDE_BALANCE] = hidden
        }
    }

    suspend fun setPrivacyCreditEnabled(enabled: Boolean) {
        store.edit { prefs ->
            prefs[PRIVACY_CREDIT_ENABLED] = enabled
        }
    }

    suspend fun setPrivacySocialMediaEnabled(enabled: Boolean) {
        store.edit { prefs ->
            prefs[PRIVACY_SOCIAL_MEDIA_ENABLED] = enabled
        }
    }

    /* ---------------- Cloud Security Cache ---------------- */
    suspend fun saveCloudSecuritySettings(settings: SecuritySettingsModel?) {
        context.securityDataStore.edit { prefs ->
            prefs[CLOUD_SETTINGS] = gson.toJson(settings)
            prefs[LOCK_AFTER_MINUTES] = settings?.lockAfterMinutes ?: 5
        }
    }

    val cloudSecuritySettingsFlow: Flow<SecuritySettingsModel?> =
        context.securityDataStore.data.map { prefs ->
            prefs[CLOUD_SETTINGS]?.let {
                gson.fromJson(it, SecuritySettingsModel::class.java)
            }
        }


    /* ---------------- SESSION ---------------- */

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
        val hasVerifiedEmailMerged = local.hasVerifiedEmail || (server?.hasVerifiedEmail == true)
        // Once set, keep recovery method ready. Treat existing users with a password
        // (local or cloud) as having already satisfied the recovery requirement (backward compat).
        val localPasswordSetOnDevice = (server?.localPasswordSetAt ?: local.localPasswordSetAt) != null
        val recoveryMethodReadyMerged = local.recoveryMethodReady
            || hasVerifiedEmailMerged
            || localPasswordSetOnDevice
            || server?.passwordEnabled == true
        val hasAddedHomeAddressMerged =
            (local.hasAddedHomeAddress == true) || (server?.hasAddedHomeAddress == true)
        val hasVerifiedIdentityMerged = local.hasVerifiedIdentity || (server?.hasVerifiedIdentity == true)
        val hasEnrolledMfaFactorMerged =
            local.hasEnrolledMfaFactor || (server?.hasEnrolledMfaFactor == true)

        return local.copy(
            recoveryMethodReady = recoveryMethodReadyMerged,
            // Server overrides requirements (e.g. what's required)
            biometricsRequired = server?.biometricsRequired ?: local.biometricsRequired,
            passwordEnabled = server?.passwordEnabled ?: local.passwordEnabled,
            passkeyEnabled = server?.passkeyEnabled ?: local.passkeyEnabled,
            biometricsEnabled = local.biometricsEnabled,
            biometricsEnabledAt = local.biometricsEnabledAt,
            localPasswordSetAt = mostRecentTimestamp(
                local.localPasswordSetAt,
                server?.localPasswordSetAt
            ),
            killSwitchActive = server?.killswitch ?: local.killSwitchActive,
            
            allowFederatedLinking = server?.allowFederatedLinking ?: local.allowFederatedLinking,

            lockAfterMinutes = server?.lockAfterMinutes ?: local.lockAfterMinutes,
            onboardingRequired = server?.onboardingRequired ?: local.onboardingRequired,
            onboardingCompleted = server?.onboardingCompleted ?: local.onboardingCompleted,

            hasVerifiedEmail = hasVerifiedEmailMerged,
            emailVerificationSentAt = server?.emailVerificationSentAt ?: local.emailVerificationSentAt,
            emailToVerify = server?.emailToVerify ?: local.emailToVerify,
            hasAddedHomeAddress = hasAddedHomeAddressMerged,
            hasVerifiedIdentity = hasVerifiedIdentityMerged,
            hasEnrolledMfaFactor = hasEnrolledMfaFactorMerged,
            mfaEnrolledAt = mostRecentTimestamp(local.mfaEnrolledAt, server?.mfaEnrolledAt),
            hasSkippedMfaEnrollmentPrompt = server?.hasSkippedMfaEnrollmentPrompt
                ?: local.hasSkippedMfaEnrollmentPrompt,
            hasSkippedPasskeyEnrollmentPrompt = server?.hasSkippedPasskeyEnrollmentPrompt
                ?: local.hasSkippedPasskeyEnrollmentPrompt,

            tosAcceptedAt = server?.tosAcceptedAt ?: local.tosAcceptedAt,
            kycStatus = server?.kycStatus ?: local.kycStatus,
            updatedAt = server?.updatedAt ?: local.updatedAt,
            lastSynced = System.currentTimeMillis()
        )
    }

    private fun mostRecentTimestamp(
        local: Timestamp?,
        remote: Timestamp?
    ): Timestamp? {
        return when {
            local == null -> remote
            remote == null -> local
            local.seconds > remote.seconds -> local
            local.seconds < remote.seconds -> remote
            local.nanoseconds >= remote.nanoseconds -> local
            else -> remote
        }
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

    /* ---------------- Lock State ---------------- */

    val isInitializedFlow: Flow<Boolean> =
        store.data.map { it[LOCAL_STATE_INITIALIZED] ?: false }

    val lastUnlockFlow: Flow<Long> =
        context.securityDataStore.data.map { it[LAST_UNLOCK] ?: 0L }

    suspend fun clear() {
        context.securityDataStore.edit { it.clear() }
    }
}
