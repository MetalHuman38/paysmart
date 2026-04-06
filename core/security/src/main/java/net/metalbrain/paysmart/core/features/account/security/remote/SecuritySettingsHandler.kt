package net.metalbrain.paysmart.core.features.account.security.remote

import android.util.Log
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import javax.inject.Inject

class SecuritySettingsHandler @Inject constructor(
    private val client: SecuritySettingsPolicy
) {

    suspend fun getSecuritySettings(idToken: String): SecuritySettingsModel? {
        return try {
            client.getServerSecuritySettings(idToken)
        } catch (e: Exception) {
            Log.e("SecuritySettingsHandler", "getSecuritySettings", e)
            null
        }
    }

}
