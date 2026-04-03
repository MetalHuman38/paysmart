package net.metalbrain.paysmart.core.features.account.passkey.repository

import android.app.Activity
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasskeyCredentialManager @Inject constructor() {

    suspend fun createPasskey(
        activity: Activity,
        requestJson: String
    ): Result<String> = runCatching {
        val credentialManager = CredentialManager.create(activity)
        // Use the fully-specified public constructor so the call site stays binary-stable
        // when the resolved runtime credentials artifact differs across app modules.
        val request = CreatePublicKeyCredentialRequest(
            requestJson = requestJson,
            clientDataHash = null,
            preferImmediatelyAvailableCredentials = false,
            origin = null,
            preferDefaultProvider = null,
            isAutoSelectAllowed = false
        )
        val response = credentialManager.createCredential(activity, request)
        val passkeyResponse = response as? CreatePublicKeyCredentialResponse
            ?: throw IllegalStateException("Passkey registration response is missing")
        passkeyResponse.registrationResponseJson
    }

    suspend fun getAssertion(
        activity: Activity,
        requestJson: String,
        preferImmediatelyAvailableCredentials: Boolean = false
    ): Result<String> = runCatching {
        val credentialManager = CredentialManager.create(activity)
        val request = GetCredentialRequest(
            credentialOptions = listOf(
                GetPublicKeyCredentialOption(
                    requestJson = requestJson,
                    clientDataHash = null,
                    allowedProviders = emptySet()
                )
            ),
            preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials
        )
        val response = credentialManager.getCredential(activity, request)
        val publicKeyCredential = response.credential as? PublicKeyCredential
            ?: throw IllegalStateException("Passkey assertion response is missing")
        publicKeyCredential.authenticationResponseJson
    }
}
