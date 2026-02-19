package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import javax.inject.Inject

class AllowFederatedLinkingHandler @Inject constructor(
) {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = AllowFederatedLinkingPolicy(
        config = config,
    )

    suspend fun allowLinking(idToken: String): Boolean {
        return try {
            client.enableAllowFederatedLinking(idToken)
        } catch (e: Exception) {
            Log.e(
                "AllowFederatedLinking",
                "Failed to enable federated linking",
                e
            )
            false
        }
    }
}
