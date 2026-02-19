package net.metalbrain.paysmart.domain.model

import com.google.firebase.Timestamp
import net.metalbrain.paysmart.domain.state.OnboardingState

data class LocalSecuritySettingsModel(
    var allowFederatedLinking: Boolean? = null,
    var biometricsRequired: Boolean = true,
    var biometricsEnabled: Boolean = false,
    var biometricsEnabledAt: Timestamp? = null,

    var passcodeEnabled: Boolean = false,
    var passwordEnabled: Boolean = false,

    var localPassCodeSetAt: Timestamp? = null,
    var localPasswordSetAt: Timestamp? = null,
    var lockAfterMinutes: Int? = 5,
    var tosAcceptedAt: Timestamp? = null,
    var kycStatus: String? = null,

    // Onboarding
    var onboardingRequired: Map<String, Boolean>? = null,
    var onboardingCompleted: Map<String, Boolean>? = null,

    var updatedAt: Timestamp? = null,

    var hasAddedHomeAddress: Boolean? = null,

    var hasVerifiedEmail: Boolean = false,
    var emailVerificationSentAt: Timestamp? = null,
    var emailToVerify: String? = null,

    var hasVerifiedIdentity: Boolean = false,
    var identityVerificationSentAt: Timestamp? = null,
    var identityToVerify: String? = null,

    var sessionLocked: Boolean = false,
    var killSwitchActive: Boolean = false,
    var lastSynced: Long = 0L
)

val LocalSecuritySettingsModel.hasCompletedEmailVerification: Boolean
    get() = hasVerifiedEmail

val LocalSecuritySettingsModel.hasCompletedAddress: Boolean
    get() = hasAddedHomeAddress == true

val LocalSecuritySettingsModel.hasCompletedIdentity: Boolean
    get() = hasVerifiedIdentity

fun LocalSecuritySettingsModel.asOnboardingState(): OnboardingState {
    return OnboardingState(
        required = onboardingRequired ?: emptyMap(),
        completed = onboardingCompleted ?: emptyMap()
    )
}
