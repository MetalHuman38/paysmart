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
    val confirmPhoneChanged: String = "/auth/confirmPhoneChanged",
    val lookupAddress: String = "/auth/lookupAddress",
    val setHomeAddressVerified: String = "/auth/setHomeAddressVerified",
    val identityUploadSession: String = "/auth/identity/upload/session",
    val identityUploadPayload: String = "/auth/identity/upload/payload",
    val identityUploadCommit: String = "/auth/identity/upload/commit",
    val identityExtractText: String = "/auth/identity/extractText",
    val identityImageAttestation: String = "/auth/identity/image/attestation",
    val identityProviderStart: String = "/auth/identity/provider/startSession",
    val identityProviderResume: String = "/auth/identity/provider/resume",
    val identityProviderCallback: String = "/auth/identity/provider/callback",
    val passkeyRegisterOptions: String = "/auth/passkeys/register/options",
    val passkeyRegisterVerify: String = "/auth/passkeys/register/verify",
    val passkeyAuthenticateOptions: String = "/auth/passkeys/authenticate/options",
    val passkeyAuthenticateVerify: String = "/auth/passkeys/authenticate/verify",
    val addMoneySession: String = "/payments/add-money/session",
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

    val confirmPhoneChangedUrl get() = "$apiBase$confirmPhoneChanged"

    val lookupAddressUrl get() = "$apiBase$lookupAddress"

    val setHomeAddressVerifiedUrl get() = "$apiBase$setHomeAddressVerified"

    val identityUploadSessionUrl get() = "$apiBase$identityUploadSession"

    val identityUploadCommitUrl get() = "$apiBase$identityUploadCommit"

    val identityUploadPayloadUrl get() = "$apiBase$identityUploadPayload"

    val identityExtractTextUrl get() = "$apiBase$identityExtractText"

    val identityImageAttestationUrl get() = "$apiBase$identityImageAttestation"

    val identityProviderStartUrl get() = "$apiBase$identityProviderStart"

    val identityProviderResumeUrl get() = "$apiBase$identityProviderResume"

    val identityProviderCallbackUrl get() = "$apiBase$identityProviderCallback"

    val passkeyRegisterOptionsUrl get() = "$apiBase$passkeyRegisterOptions"

    val passkeyRegisterVerifyUrl get() = "$apiBase$passkeyRegisterVerify"

    val passkeyAuthenticateOptionsUrl get() = "$apiBase$passkeyAuthenticateOptions"

    val passkeyAuthenticateVerifyUrl get() = "$apiBase$passkeyAuthenticateVerify"

    val addMoneySessionUrl get() = "$apiBase$addMoneySession"

    fun addMoneySessionStatusUrl(sessionId: String): String {
        return "$apiBase$addMoneySession/${sessionId.trim()}"
    }

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
