package net.metalbrain.paysmart.domain.model

import net.metalbrain.paysmart.domain.state.OnboardingState
import com.google.firebase.Timestamp

data class SecuritySettings(
    val passcodeEnabled: Boolean? = false,
    val passwordEnabled: Boolean? = false,
    val biometricsRequired: Boolean? = false,
    val hasAddedHomeAddress: Boolean? = null,
    // Email verification (UI-facing only)
    val hasVerifiedEmail: Boolean = false,
    val emailVerificationSentAt: Timestamp? = null,
    var emailToVerify: String? = null,
    var emailVerificationAttemptsToday: Int = 0,

    // Identity verification control
    val hasVerifiedIdentity: Boolean? = null,

    // Local security control
    val localPassCodeSetAt: Timestamp? = null,
    val localPasswordSetAt: Timestamp? = null,
    val lockAfterMinutes: Int? = 5,

    // Compliance control
    val tosAcceptedAt: Timestamp? = null,
    val kycStatus: String? = null,

    // Onboarding control
    val onboardingRequired: Map<String, Boolean>? = null,
    val onboardingCompleted: Map<String, Boolean>? = null,
    val updatedAt: Timestamp? = null,
)

fun SecuritySettings.asOnboardingState(): OnboardingState {
    return OnboardingState(
        required = onboardingRequired ?: emptyMap(),
        completed = onboardingCompleted ?: emptyMap()
    )
}
