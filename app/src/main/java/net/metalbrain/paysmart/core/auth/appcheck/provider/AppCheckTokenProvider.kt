package net.metalbrain.paysmart.core.auth.appcheck.provider

import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.BuildConfig

interface AppCheckTokenProvider {
    suspend fun getTokenOrNull(forceRefresh: Boolean = false): String?
}

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
