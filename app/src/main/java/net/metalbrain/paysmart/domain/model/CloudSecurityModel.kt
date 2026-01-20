package net.metalbrain.paysmart.domain.model

import net.metalbrain.paysmart.domain.state.OnboardingState
import com.google.firebase.Timestamp
import androidx.annotation.Keep

@Keep
enum class UserStatus {
    Unverified,
    Verified,
    KycPending
}

data class SecuritySettings(
    // Core auth
    var passcodeEnabled: Boolean? = false,
    var passwordEnabled: Boolean? = false,
    var biometricsRequired: Boolean? = true,

    // Address
    var hasAddedHomeAddress: Boolean? = null,


    // Email verification (UI-facing only)
    var hasVerifiedEmail: Boolean = false,
    var emailVerificationSentAt: Timestamp? = null,
    var emailToVerify: String? = null,
    var emailVerificationAttemptsToday: Int = 0,

    // Identity verification
    var hasVerifiedIdentity: Boolean? = null,

    // Local security
    var localPassCodeSetAt: Timestamp? = null,
    var localPasswordSetAt: Timestamp? = null,
    var lockAfterMinutes: Int? = 5,

    // Compliance
    var tosAcceptedAt: Timestamp? = null,
    var kycStatus: String? = null,

    // Onboarding
    var onboardingRequired: Map<String, Boolean>? = null,
    var onboardingCompleted: Map<String, Boolean>? = null,

    // Metadata
    var updatedAt: Timestamp? = null,
)

fun SecuritySettings.deriveUserStatus(): UserStatus {
    return when {
        hasVerifiedIdentity == true -> UserStatus.KycPending
        hasVerifiedEmail -> UserStatus.Verified
        else -> UserStatus.Unverified
    }
}

val SecuritySettings.hasCompletedEmailVerification: Boolean
    get() = hasVerifiedEmail

val SecuritySettings.hasCompletedAddress: Boolean
    get() = hasAddedHomeAddress == true

val SecuritySettings.hasCompletedIdentity: Boolean
    get() = hasVerifiedIdentity == true

fun SecuritySettings.asOnboardingState(): OnboardingState {
    return OnboardingState(
        required = onboardingRequired ?: emptyMap(),
        completed = onboardingCompleted ?: emptyMap()
    )
}
