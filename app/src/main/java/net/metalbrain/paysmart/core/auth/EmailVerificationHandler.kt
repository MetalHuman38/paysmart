package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import javax.inject.Inject

class EmailVerificationHandler @Inject constructor() {

    private val client = EmailVerificationClient(
        AuthApiConfig(
            baseUrl = Env.apiBase,
            attachApiPrefix = true
        )
    )

    suspend fun sendVerification(idToken: String, email: String): Boolean =
        runCatching {
            client.generateVerification(idToken, email)
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
