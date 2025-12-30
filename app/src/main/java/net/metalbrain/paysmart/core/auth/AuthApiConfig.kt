package net.metalbrain.paysmart.core.auth

data class AuthApiConfig(
    val baseUrl: String,
    val checkPhoneOrEmail: String = "/auth/check-email-or-phone",
    val checkIfPhoneAlreadyExist: String = "/auth/check-phone",
    val setPasswordEnabled: String = "/auth/setPasswordEnabled",
    val getPasswordEnabled: String = "/auth/getPasswordEnabled",
    val generateEmailVerificationHandler: String = "/auth/generate",
    val checkEmailVerificationStatusHandler: String = "/auth/status",
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

    val setPasswordEnabledUrl get() = "$apiBase$setPasswordEnabled"

    val getPasswordEnabledUrl get() = "$apiBase$getPasswordEnabled"

    val generateEmailVerificationHandlerUrl get() = "$apiBase$generateEmailVerificationHandler"

    val checkEmailVerificationStatusHandlerUrl get() = "$apiBase$checkEmailVerificationStatusHandler"
    
    val usersEnsureUrl get() = "$apiBase$usersEnsurePath"

    companion object {
        fun withApiPrefix(baseUrl: String): AuthApiConfig {
            return AuthApiConfig(baseUrl = baseUrl, attachApiPrefix = true)
        }
    }
}
