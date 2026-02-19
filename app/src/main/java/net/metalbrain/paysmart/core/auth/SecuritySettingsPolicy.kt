package net.metalbrain.paysmart.core.auth


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import okhttp3.*
import org.json.JSONObject
import com.google.firebase.Timestamp

class SecuritySettingsPolicy(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    private fun parseFirestoreTimestamp(jsonObject: JSONObject?): Timestamp? {
        if (jsonObject == null) return null

        return try {
            val seconds = when {
                jsonObject.has("_seconds") -> jsonObject.optLong("_seconds", -1)
                jsonObject.has("seconds") -> jsonObject.optLong("seconds", -1)
                else -> -1
            }
            val nanos = when {
                jsonObject.has("_nanoseconds") -> jsonObject.optInt("_nanoseconds", 0)
                jsonObject.has("nanoseconds") -> jsonObject.optInt("nanoseconds", 0)
                else -> 0
            }
            if (seconds == -1L) null else Timestamp(seconds, nanos)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun optNullableBoolean(json: JSONObject, key: String): Boolean? {
        if (!json.has(key) || json.isNull(key)) return null
        return json.optBoolean(key)
    }

    private fun optNullableInt(json: JSONObject, key: String): Int? {
        if (!json.has(key) || json.isNull(key)) return null
        return json.optInt(key)
    }

    private fun optNullableString(json: JSONObject, key: String): String? {
        if (!json.has(key) || json.isNull(key)) return null
        val value = json.optString(key, "")
        return if (value.isBlank()) null else value
    }

    private fun optNullableBooleanMap(json: JSONObject, key: String): Map<String, Boolean>? {
        if (!json.has(key) || json.isNull(key)) return null
        val nested = json.optJSONObject(key) ?: return null
        val result = linkedMapOf<String, Boolean>()
        val iterator = nested.keys()
        while (iterator.hasNext()) {
            val entryKey = iterator.next()
            if (!nested.isNull(entryKey)) {
                result[entryKey] = nested.optBoolean(entryKey, false)
            }
        }
        return result
    }

    private fun parseSecuritySettings(json: JSONObject): SecuritySettingsModel {
        return SecuritySettingsModel(
            allowFederatedLinking = optNullableBoolean(json, "allowFederatedLinking"),
            killswitch = optNullableBoolean(json, "killswitch") ?: false,
            biometricsRequired = optNullableBoolean(json, "biometricsRequired"),
            biometricsEnabled = optNullableBoolean(json, "biometricsEnabled"),
            passcodeEnabled = optNullableBoolean(json, "passcodeEnabled"),
            localPassCodeSetAt = parseFirestoreTimestamp(json.optJSONObject("localPassCodeSetAt")),
            localPasswordSetAt = parseFirestoreTimestamp(json.optJSONObject("localPasswordSetAt")),
            passwordEnabled = optNullableBoolean(json, "passwordEnabled"),
            biometricsEnabledAt = parseFirestoreTimestamp(json.optJSONObject("biometricsEnabledAt")),
            lockAfterMinutes = optNullableInt(json, "lockAfterMinutes"),
            hasVerifiedEmail = optNullableBoolean(json, "hasVerifiedEmail") ?: false,
            hasAddedHomeAddress = optNullableBoolean(json, "hasAddedHomeAddress"),
            hasVerifiedIdentity = optNullableBoolean(json, "hasVerifiedIdentity"),
            emailVerificationSentAt = parseFirestoreTimestamp(json.optJSONObject("emailVerificationSentAt")),
            emailToVerify = optNullableString(json, "emailToVerify"),
            emailVerificationAttemptsToday = optNullableInt(json, "emailVerificationAttemptsToday") ?: 0,
            tosAcceptedAt = parseFirestoreTimestamp(json.optJSONObject("tosAcceptedAt")),
            kycStatus = optNullableString(json, "kycStatus"),
            onboardingRequired = optNullableBooleanMap(json, "onboardingRequired"),
            onboardingCompleted = optNullableBooleanMap(json, "onboardingCompleted"),
            updatedAt = parseFirestoreTimestamp(json.optJSONObject("updatedAt"))
        )
    }

    /**
     * Get server security settings.
     */
    suspend fun getServerSecuritySettings(idToken: String): SecuritySettingsModel? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(config.getSecuritySettingsUrl)
            .header("Authorization", "Bearer $idToken")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string()
            if (body == null) {
                Log.w("ServerSecuritySettings", "getServerSecuritySettings: empty body")
                return@withContext null
            }

            try {
                val root = JSONObject(body)
                val settings = root.optJSONObject("settings") ?: root
                return@withContext parseSecuritySettings(settings)

            } catch (e: Exception) {
                Log.e("ServerSecuritySettings", "getServerSecuritySettings: JSON parse error", e)
                return@withContext null
            }
        }

    }
}
