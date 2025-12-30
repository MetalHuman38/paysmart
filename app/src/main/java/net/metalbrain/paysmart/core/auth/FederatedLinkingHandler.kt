package net.metalbrain.paysmart.core.auth

import jakarta.inject.Inject
import net.metalbrain.paysmart.Env

class FederatedLinkingHandler @Inject constructor() {
    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = FederatedLinkingPolicy(config)

    suspend fun checkFederatedAccountExists(email: String?, phone: String?): List<String> {
        return try {
            client.checkIfFederatedAccountExists(email, phone)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
