package net.metalbrain.paysmart.core.features.account.security.mfa.data

data class MfaEnrollmentStatus(
    val signedIn: Boolean,
    val emailVerified: Boolean,
    val hasEnrolledFactor: Boolean,
    val supportsEnrollment: Boolean = true,
    val enrollmentBlockMessage: String? = null,
    val blockedActionLabel: String? = null
)
