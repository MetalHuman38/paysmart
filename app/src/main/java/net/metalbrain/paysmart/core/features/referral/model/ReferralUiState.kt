package net.metalbrain.paysmart.core.features.referral.model

data class ReferralUiState(
    val enteredCode: String = "",
    val rewardLabel: String = "10.00 GBP",
    val transferThresholdLabel: String = "100.00 GBP",
    val isSubmitting: Boolean = false
) {
    val canSubmit: Boolean
        get() = enteredCode.trim().isNotEmpty() && !isSubmitting
}
