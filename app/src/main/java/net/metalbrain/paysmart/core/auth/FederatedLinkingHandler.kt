package net.metalbrain.paysmart.core.auth

import jakarta.inject.Inject
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider

class FederatedLinkingHandler @Inject constructor(
    appCheckTokenProvider: AppCheckTokenProvider
) {
    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

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
