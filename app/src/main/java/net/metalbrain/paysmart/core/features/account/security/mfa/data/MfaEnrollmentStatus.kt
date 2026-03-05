package net.metalbrain.paysmart.core.features.account.security.mfa.data

data class MfaEnrollmentStatus(
    val signedIn: Boolean,
    val emailVerified: Boolean,
    val hasEnrolledFactor: Boolean
)
