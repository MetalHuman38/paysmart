package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import javax.inject.Inject

class SecuritySettingsHandler @Inject constructor() {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = SecuritySettingsPolicy(config)

    suspend fun getSecuritySettings(idToken: String): SecuritySettingsModel? {
        return try {
            client.getServerSecuritySettings(idToken)
        } catch (e: Exception) {
            Log.e("SecuritySettingsHandler", "getSecuritySettings", e)
            null
        }
    }

}
