package net.metalbrain.paysmart.core.features.account.security.mfa.data

data class MfaSignInFactorOption(
    val factorUid: String,
    val displayName: String?,
    val destinationHint: String
)
