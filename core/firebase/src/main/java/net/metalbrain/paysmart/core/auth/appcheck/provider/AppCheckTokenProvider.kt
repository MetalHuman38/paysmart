package net.metalbrain.paysmart.core.auth.appcheck.provider

import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.common.runtime.RuntimeConfig
import javax.inject.Inject
import javax.inject.Singleton

interface AppCheckTokenProvider {
    suspend fun getTokenOrNull(forceRefresh: Boolean = false): String?
}

@Singleton
class FirebaseAppCheckTokenProvider @Inject constructor(
    private val firebaseAppCheck: FirebaseAppCheck,
    private val runtimeConfig: RuntimeConfig
) : AppCheckTokenProvider {

    override suspend fun getTokenOrNull(forceRefresh: Boolean): String? {
        if (runtimeConfig.isLocal) {
            return null
        }

        return runCatching {
            firebaseAppCheck.getAppCheckToken(forceRefresh).await().token.trim()
        }.fold(
            onSuccess = { token ->
                if (token.isNotEmpty()) {
                    token
                } else if (runtimeConfig.appCheckEnforced) {
                    throw IllegalStateException("App Check token is empty")
                } else {
                    null
                }
            },
            onFailure = { error ->
                if (runtimeConfig.appCheckEnforced) {
                    throw IllegalStateException("Unable to fetch App Check token", error)
                }
                Log.w("AppCheckTokenProvider", "App Check unavailable in optional mode", error)
                null
            }
        )
    }
}
