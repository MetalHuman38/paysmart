package net.metalbrain.paysmart.core.auth

import jakarta.inject.Inject
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig

class FederatedLinkingHandler @Inject constructor(
    appCheckTokenProvider: AppCheckTokenProvider,
    @ApiPrefixedAuthConfig config: AuthApiConfig
) {
    private val client = FederatedLinkingPolicy(
        config = config,
        appCheckTokenProvider = appCheckTokenProvider
    )

    suspend fun checkFederatedAccountExists(email: String?, phone: String?): List<String> {
        return try {
            client.checkIfFederatedAccountExists(email, phone)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
