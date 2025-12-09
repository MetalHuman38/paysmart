package net.metalbrain.paysmart.ui.home

data class ProfileSetupState(
    val hasVerifiedEmail: Boolean = false,
    val hasAddedHomeAddress: Boolean = false,
    val hasVerifiedIdentity: Boolean = false
) {
    val completedSteps: Int
        get() = listOf(
            hasVerifiedEmail,
            hasAddedHomeAddress,
            hasVerifiedIdentity
        ).count { it }

    val totalSteps: Int = 3
}
