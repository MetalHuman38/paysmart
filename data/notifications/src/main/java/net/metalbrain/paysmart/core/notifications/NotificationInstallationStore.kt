package net.metalbrain.paysmart.core.notifications

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class NotificationInstallationStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun installationId(): String {
        val existing = prefs.getString(KEY_INSTALLATION_ID, null)?.trim().orEmpty()
        if (existing.isNotEmpty()) {
            return existing
        }

        val generated = UUID.randomUUID().toString()
        prefs.edit { putString(KEY_INSTALLATION_ID, generated) }
        return generated
    }

    fun getCachedFcmToken(): String? {
        return prefs.getString(KEY_CACHED_FCM_TOKEN, null)?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun setCachedFcmToken(token: String) {
        prefs.edit { putString(KEY_CACHED_FCM_TOKEN, token.trim()) }
    }

    fun getLastUploadFingerprint(): String? {
        return prefs.getString(KEY_LAST_UPLOAD_FINGERPRINT, null)?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun setLastUploadFingerprint(value: String) {
        prefs.edit { putString(KEY_LAST_UPLOAD_FINGERPRINT, value) }
    }

    fun hasShownPermissionPrompt(): Boolean {
        return prefs.getBoolean(KEY_PERMISSION_PROMPT_SHOWN, false)
    }

    fun markPermissionPromptShown() {
        prefs.edit { putBoolean(KEY_PERMISSION_PROMPT_SHOWN, true) }
    }

    private companion object {
        const val PREFS_NAME = "notification_installation_store"
        const val KEY_INSTALLATION_ID = "installation_id"
        const val KEY_CACHED_FCM_TOKEN = "cached_fcm_token"
        const val KEY_LAST_UPLOAD_FINGERPRINT = "last_upload_fingerprint"
        const val KEY_PERMISSION_PROMPT_SHOWN = "permission_prompt_shown"
    }
}
