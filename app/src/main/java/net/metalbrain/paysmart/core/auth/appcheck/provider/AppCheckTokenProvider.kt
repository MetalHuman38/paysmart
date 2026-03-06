package net.metalbrain.paysmart.core.auth.appcheck.provider

import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.BuildConfig

/**
 * Provider for Firebase App Check tokens used to verify the integrity of the application
 * and the device when making requests to backend services.
 */
interface AppCheckTokenProvider {
    suspend fun getTokenOrNull(forceRefresh: Boolean = false): String?
}

/**
 * Implementation of [AppCheckTokenProvider] that retrieves tokens from [FirebaseAppCheck].
 *
 * This provider handles the logic for fetching App Check tokens while considering the
 * application's environment and enforcement requirements:
 * - Returns `null` immediately if [BuildConfig.IS_LOCAL] is true.
 * - Throws an [IllegalStateException] if the token is empty or retrieval fails when
 *   [BuildConfig.APP_CHECK_ENFORCED] is true.
 * - Logs a warning and returns `null` if retrieval fails when enforcement is disabled.
 *
 * @property firebaseAppCheck The [FirebaseAppCheck] instance used to interact with the Firebase SDK.
 */
@Singleton
class FirebaseAppCheckTokenProvider @Inject constructor(
    private val firebaseAppCheck: FirebaseAppCheck
) : AppCheckTokenProvider {

    override suspend fun getTokenOrNull(forceRefresh: Boolean): String? {
        if (BuildConfig.IS_LOCAL) {
            return null
        }

        return runCatching {
            firebaseAppCheck.getAppCheckToken(forceRefresh).await().token.trim()
        }.fold(
            onSuccess = { token ->
                if (token.isNotEmpty()) {
                    token
                } else if (BuildConfig.APP_CHECK_ENFORCED) {
                    throw IllegalStateException("App Check token is empty")
                } else {
                    null
                }
            },
            onFailure = { error ->
                if (BuildConfig.APP_CHECK_ENFORCED) {
                    throw IllegalStateException("Unable to fetch App Check token", error)
                }
                Log.w("AppCheckTokenProvider", "App Check unavailable in optional mode", error)
                null
            }
        )
    }
}
