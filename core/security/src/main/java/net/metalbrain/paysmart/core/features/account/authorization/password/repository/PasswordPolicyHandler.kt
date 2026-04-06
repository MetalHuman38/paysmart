package net.metalbrain.paysmart.core.features.account.authorization.password.repository

import android.util.Log
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.common.runtime.RuntimeConfig
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import javax.inject.Inject

class PasswordPolicyHandler @Inject constructor(
    @ApiPrefixedAuthConfig config: AuthApiConfig,
    runtimeConfig: RuntimeConfig
) {
    private val client = PasswordPolicyClient(
        config = config,
        debugLoggingEnabled = runtimeConfig.isDebug
    )

    suspend fun setPasswordEnabled(idToken: String): Boolean {
        return try {
            client.markPasswordEnabled(idToken)
        } catch (e: Exception) {
            Log.e("PasswordPolicyHandler", "Failed to call setPasswordEnabled", e)
            false
        }
    }

    suspend fun getPasswordEnabled(idToken: String): Boolean {
        return try {
            client.isPasswordEnabled(idToken)
        } catch (e: Exception) {
            Log.e("PasswordPolicyHandler", "Failed to call isPasswordEnabled", e)
            false
        }
    }

}
