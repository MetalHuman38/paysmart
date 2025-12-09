package net.metalbrain.paysmart.domain.model

import java.security.Timestamp

data class SecuritySettings(
    val passcodeEnabled: Boolean? = false,
    val biometricsRequired: Boolean? = false,
    val lockAfterMinutes: Int? = 5,
    val tosAcceptedAt: Timestamp? = null,
    val kycStatus: String? = null,
    val onboardingRequired: Map<String, Boolean>? = null,
    val onboardingCompleted: Map<String, Boolean>? = null,
    val updatedAt: Timestamp? = null
)
