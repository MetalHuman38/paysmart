package net.metalbrain.paysmart.core.features.identity.handoff

data class IdentityProviderSessionStart(
    val countryIso2: String? = null,
    val documentType: String? = null
)

data class IdentityProviderSession(
    val sessionId: String,
    val provider: String,
    val status: String,
    val launchUrl: String? = null,
    val expiresAtMs: Long? = null
)

data class IdentityProviderSessionResume(
    val sessionId: String,
    val provider: String,
    val status: String,
    val launchUrl: String? = null,
    val reason: String? = null,
    val updatedAtMs: Long? = null
)

data class IdentityProviderSdkCallback(
    val event: String,
    val sessionId: String? = null,
    val providerRef: String? = null,
    val rawDeepLink: String? = null
)

interface IdentityProviderHandoffRepository {
    suspend fun startSession(
        request: IdentityProviderSessionStart
    ): Result<IdentityProviderSession>

    suspend fun resumeSession(
        sessionId: String
    ): Result<IdentityProviderSessionResume>

    suspend fun submitSdkCallback(
        callback: IdentityProviderSdkCallback
    ): Result<IdentityProviderSessionResume>
}
