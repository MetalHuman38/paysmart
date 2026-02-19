package net.metalbrain.paysmart.core.auth

data class AuthApiConfig (
    val baseUrl: String,
    val checkPhoneOrEmail: String = "/auth/check-email-or-phone",
    val checkIfPhoneAlreadyExist: String = "/auth/check-phone",
    val allowFederatedLinking: String = "/auth/allowFederatedLinking",
    val setBiometricEnabled: String = "/auth/setBiometricEnabled",
    val getBiometricEnabled: String = "/auth/getBiometricEnabled",
    val setPasswordEnabled: String = "/auth/setPasswordEnabled",
    val getPasswordEnabled: String = "/auth/getPasswordEnabled",
    val setPassCodeEnabled: String = "/auth/setPassCodeEnabled",
    val getPassCodeEnabled: String = "/auth/getPassCodeEnabled",
    val generateEmailVerificationHandler: String = "/auth/generate",
    val checkEmailVerificationStatusHandler: String = "/auth/status",
    val getSecuritySettings: String = "/auth/getSecuritySettings",
    val usersEnsurePath: String = "/users/ensure",
    val attachApiPrefix: Boolean = false
) {
    val apiBase: String
        get() {
            val root = baseUrl.trimEnd('/')
            return if (attachApiPrefix && !root.endsWith("/api")) "$root/api" else root
        }

    val checkPhoneOrEmailUrl get() = "$apiBase$checkPhoneOrEmail"
    
    val checkIfPhoneAlreadyExistUrl get() = "$apiBase$checkIfPhoneAlreadyExist"

    val allowFederatedLinkingUrl get() = "$apiBase$allowFederatedLinking"
    
    val setBiometricEnabledUrl get() = "$apiBase$setBiometricEnabled"

    val getBiometricEnabledUrl get() = "$apiBase$getBiometricEnabled"
    
    val setPasswordEnabledUrl get() = "$apiBase$setPasswordEnabled"

    val getPasswordEnabledUrl get() = "$apiBase$getPasswordEnabled"

    val setPassCodeEnabledUrl get() = "$apiBase$setPassCodeEnabled"

    val getPassCodeEnabledUrl get() = "$apiBase$getPassCodeEnabled"

    val generateEmailVerificationHandlerUrl get() = "$apiBase$generateEmailVerificationHandler"

    val checkEmailVerificationStatusHandlerUrl get() = "$apiBase$checkEmailVerificationStatusHandler"

    val getSecuritySettingsUrl get() = "$apiBase$getSecuritySettings"
    
    val usersEnsureUrl get() = "$apiBase$usersEnsurePath"

    companion object {
        fun withApiPrefix(baseUrl: String): AuthApiConfig {
            return AuthApiConfig(baseUrl = baseUrl, attachApiPrefix = true)
        }
    }
}
