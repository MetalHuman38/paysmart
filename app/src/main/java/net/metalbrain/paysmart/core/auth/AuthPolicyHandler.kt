// AuthPolicyHandler.kt
package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import javax.inject.Inject

class AuthPolicyHandler @Inject constructor(
    appCheckTokenProvider: AppCheckTokenProvider
) {

    private val config = AuthApiConfig(
        baseUrl = Env.authBase,
        attachApiPrefix = false
    )

    private val client = AuthPolicyClient(
        config = config,
        requireAppCheckToken = config.checkIfPhoneAlreadyExistUrl.startsWith(
            prefix = "https://",
            ignoreCase = true
        ),
        appCheckTokenProvider = appCheckTokenProvider
    )

    suspend fun isPhoneAlreadyRegistered(e164: String): Boolean {
        return try {
            client.checkIfPhoneNumberAlreadyExist(e164)
        } catch (e: Exception) {
            Log.e("AuthPolicyHandler", "Phone check failed", e)
            throw IllegalStateException(
                "Unable to verify phone availability. Please retry.",
                e
            )
        }
    }
}
