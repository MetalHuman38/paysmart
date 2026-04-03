package net.metalbrain.paysmart.core.features.account.authentication.email.remote

import android.util.Log
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import javax.inject.Inject

class EmailVerificationHandler @Inject constructor(
    @ApiPrefixedAuthConfig config: AuthApiConfig
) {

    private val client = EmailVerificationClient(config)

    suspend fun sendVerification(
        idToken: String,
        email: String,
        returnRoute: String? = null
    ): Boolean =
        runCatching {
            client.generateVerification(idToken, email, returnRoute)
        }.onFailure {
            Log.e("EmailVerification", "generate failed", it)
        }.getOrDefault(false)

    suspend fun isVerified(idToken: String, email: String): Boolean =
        runCatching {
            client.checkVerificationStatus(idToken, email)
        }.onFailure {
            Log.e("EmailVerification", "status check failed", it)
        }.getOrDefault(false)
}
