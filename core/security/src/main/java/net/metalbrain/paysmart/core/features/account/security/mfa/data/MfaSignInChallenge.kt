package net.metalbrain.paysmart.core.features.account.security.mfa.data

data class MfaSignInChallenge(
    val factors: List<MfaSignInFactorOption>,
    val selectedFactorUid: String,
    val destinationHint: String
)
