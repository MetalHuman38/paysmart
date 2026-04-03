package net.metalbrain.paysmart.core.features.account.security.mfa.remote

import android.util.Log
import jakarta.inject.Inject
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig

class MfaEnrollmentPromptPolicyHandler @Inject constructor(
    @ApiPrefixedAuthConfig config: AuthApiConfig
) {
    private val client = MfaEnrollmentPromptPolicyClient(config)

    suspend fun setPromptState(idToken: String, hasSkippedMfaEnrollmentPrompt: Boolean): Boolean {
        return try {
            client.setPromptState(idToken, hasSkippedMfaEnrollmentPrompt)
        } catch (e: Exception) {
            Log.e(
                "MfaPromptPolicy",
                "Failed to sync hasSkippedMfaEnrollmentPrompt",
                e
            )
            false
        }
    }
}
