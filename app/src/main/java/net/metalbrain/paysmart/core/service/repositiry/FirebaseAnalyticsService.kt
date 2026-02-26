package net.metalbrain.paysmart.core.service.repositiry

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.getValue

enum class EventType(val value: String) {
    LOGIN("login"),
}

@Singleton
class FirebaseAnalyticsService @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : FirebaseAnalyticalServiceInterface {

    private val crashlytics by lazy { FirebaseCrashlytics.getInstance() }

    override fun logEvent(event: String, params: Map<String, Any?>) {
        val eventName = sanitizeKey(event)
        if (eventName.isBlank()) return
        firebaseAnalytics.logEvent(eventName, params.toBundle())
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to sanitizeKey(screenName),
                FirebaseAnalytics.Param.SCREEN_CLASS to
                    sanitizeKey(screenClass ?: screenName)
            )
        )
    }

    override fun setUserID(uid: String) {
        firebaseAnalytics.setUserId(uid)
        firebaseAnalytics.setUserProperty("user_id", uid)
        crashlytics.setUserId(uid)
    }

    override fun setUserProperty(name: String, value: String) {
        val key = sanitizeKey(name)
        if (key.isBlank()) return
        firebaseAnalytics.setUserProperty(key, value.take(MAX_VALUE_LEN))
    }

    override fun logNonFatal(
        tag: String,
        throwable: Throwable,
        extras: Map<String, String>
    ) {
        val safeTag = sanitizeKey(tag)
        if (safeTag.isNotBlank()) {
            crashlytics.setCustomKey("analytics_tag", safeTag)
        }
        extras.forEach { (key, value) ->
            val safeKey = sanitizeKey(key)
            if (safeKey.isNotBlank()) {
                crashlytics.setCustomKey(safeKey, value.take(MAX_VALUE_LEN))
            }
        }
        crashlytics.recordException(throwable)
    }
}

private const val MAX_KEY_LEN = 40
private const val MAX_VALUE_LEN = 100

private fun sanitizeKey(raw: String): String {
    val normalized = raw
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9_]"), "_")
        .replace(Regex("_+"), "_")
        .trim('_')

    return normalized.take(MAX_KEY_LEN)
}

private fun Map<String, Any?>.toBundle(): Bundle {
    val bundle = Bundle()
    forEach { (rawKey, value) ->
        val key = sanitizeKey(rawKey)
        if (key.isBlank() || value == null) return@forEach

        when (value) {
            is String -> bundle.putString(key, value.take(MAX_VALUE_LEN))
            is Int -> bundle.putInt(key, value)
            is Long -> bundle.putLong(key, value)
            is Float -> bundle.putFloat(key, value)
            is Double -> bundle.putDouble(key, value)
            is Boolean -> bundle.putBoolean(key, value)
            else -> bundle.putString(key, value.toString().take(MAX_VALUE_LEN))
        }
    }
    return bundle
}
