package net.metalbrain.paysmart.core.features.account.authorization.passcode.remote

import android.util.Log
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.common.runtime.RuntimeConfig
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import javax.inject.Inject

class PassCodePolicyHandler @Inject constructor(
    @ApiPrefixedAuthConfig config: AuthApiConfig,
    runtimeConfig: RuntimeConfig
) {
    private val client = PassCodePolicyClient(
        config = config,
        debugLoggingEnabled = runtimeConfig.isDebug
    )

    suspend fun setPassCodeEnabled(idToken: String): Boolean {
        return try {
            client.markPassCodeEnabled(idToken)
        } catch (e: Exception) {
            Log.e("PassCodePolicyHandler", "Failed to call setPassCodeEnabled", e)
            false
        }
    }

    suspend fun getPassCodeEnabled(idToken: String): Boolean {
        return try {
            client.isPassCodeEnabled(idToken)
        } catch (e: Exception) {
            Log.e("PassCodePolicyHandler", "Failed to call isPassCodeEnabled", e)
            false
        }
    }

}



//package net.metalbrain.paysmart.core.auth
//
//import android.util.Log
//import javax.inject.Inject
//
//class PassCodePolicyHandler @Inject constructor(
//    private val client: PassCodePolicyClient
//) {
//
////    private val client = PassCodePolicyClient(config)
//    suspend fun setPassCodeEnabled(idToken: String): Boolean {
//        return try {
//            client.markPassCodeEnabled(idToken)
//        } catch (e: Exception) {
//            Log.e("PasscodePolicyHandler", "Failed to call setPassCodeEnabled", e)
//            false
//        }
//    }
//
//    suspend fun getPassCodeEnabled(idToken: String): Boolean {
//        return try {
//            client.isPassCodeEnabled(idToken)
//        } catch (e: Exception) {
//            Log.e("PassCodePolicyHandler", "Failed to call isPasscodeEnabled", e)
//            false
//        }
//    }
//
//}
