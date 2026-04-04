package net.metalbrain.paysmart.core.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.Manifest
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.auth.appcheck.provider.attachRequiredAppCheckToken
import net.metalbrain.paysmart.core.common.runtime.AppVersionInfo
import net.metalbrain.paysmart.core.locale.LocaleManager
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import net.metalbrain.paysmart.data.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Singleton
class NotificationInstallationRegistrar @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val firebaseMessaging: FirebaseMessaging,
    private val appCheckTokenProvider: AppCheckTokenProvider,
    private val notificationInstallationStore: NotificationInstallationStore,
    private val okHttpClient: OkHttpClient,
    private val appVersionInfo: AppVersionInfo,
    @ApiPrefixedAuthConfig private val authApiConfig: AuthApiConfig,
) {
    private val jsonMediaType = "application/json".toMediaType()

    suspend fun cacheAndRegisterToken(token: String) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            return
        }

        notificationInstallationStore.setCachedFcmToken(cleanToken)
        registerCurrentInstallation(force = true)
    }

    suspend fun registerCurrentInstallation(force: Boolean = false) {
        val session = authRepository.getCurrentSession() ?: return
        val fcmToken = resolveFcmToken() ?: return
        val locale = LocaleManager.getSavedLanguage(context)
        val permissionGranted = notificationsPermissionGranted()
        val fingerprint = listOf(
            session.user.uid,
            fcmToken,
            locale,
            permissionGranted.toString(),
            appVersionInfo.versionName,
        ).joinToString("|")

        if (!force && fingerprint == notificationInstallationStore.getLastUploadFingerprint()) {
            return
        }

        val requestBody = JSONObject()
            .put("installationId", notificationInstallationStore.installationId())
            .put("fcmToken", fcmToken)
            .put("locale", locale)
            .put("appVersion", appVersionInfo.versionName)
            .put("notificationsPermissionGranted", permissionGranted)
            .toString()
            .toRequestBody(jsonMediaType)

        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(authApiConfig.registerNotificationInstallationUrl)
                .header("Authorization", "Bearer ${session.idToken}")
                .attachRequiredAppCheckToken(
                    appCheckTokenProvider = appCheckTokenProvider,
                    endpointName = "notifications/installations/register"
                )
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val detail = response.body?.string().orEmpty().ifBlank { "unknown_error" }
                    throw IllegalStateException(
                        "Notification installation registration failed (http=${response.code}, detail=$detail)"
                    )
                }
            }
        }

        notificationInstallationStore.setLastUploadFingerprint(fingerprint)
    }

    fun notificationsPermissionGranted(): Boolean {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private suspend fun resolveFcmToken(): String? {
        val cached = notificationInstallationStore.getCachedFcmToken()
        if (!cached.isNullOrBlank()) {
            return cached
        }

        val token = runCatching { firebaseMessaging.token.await().trim() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }

        if (token != null) {
            notificationInstallationStore.setCachedFcmToken(token)
        }
        return token
    }
}
