// AuthPolicyHandler.kt
package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.runtime.di.ApiRootAuthConfig
import javax.inject.Inject

class AuthPolicyHandler @Inject constructor(
    appCheckTokenProvider: AppCheckTokenProvider,
    @ApiRootAuthConfig config: AuthApiConfig
) {

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

    suspend fun finalizePhoneSignup(idToken: String): Boolean {
        return try {
            client.finalizePhoneSignup(idToken)
        } catch (e: Exception) {
            Log.e("AuthPolicyHandler", "Phone signup finalization failed", e)
            throw IllegalStateException(
                "Unable to finish account setup. Please retry.",
                e
            )
        }
    }
}
