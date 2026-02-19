package net.metalbrain.paysmart.domain.security

import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.StateFlow
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

interface SecurityPolicyEngine {
    val currentState: StateFlow<LocalSecuritySettingsModel>

    suspend fun evaluateSecurityPolicy(): Boolean
    suspend fun lockSession()
    suspend fun unlockSession()

    suspend fun promptBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit, 
        onFailure: (() -> Unit)? = null
    )


    fun isKillSwitchActive(): Boolean

    fun isBiometricRequired(): Boolean
    fun isPasscodeRequired(): Boolean
}
