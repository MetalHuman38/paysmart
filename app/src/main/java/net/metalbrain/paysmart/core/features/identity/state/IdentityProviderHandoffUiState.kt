package net.metalbrain.paysmart.core.features.identity.state

data class IdentityProviderHandoffUiState(
    val isStartingSession: Boolean = false,
    val isResuming: Boolean = false,
    val isSubmittingCallback: Boolean = false,
    val sessionId: String? = null,
    val provider: String = "third_party",
    val status: String = "idle",
    val launchUrl: String? = null,
    val lastEvent: String? = null,
    val reason: String? = null,
    val info: String? = null,
    val error: String? = null
) {
    val hasSession: Boolean
        get() = !sessionId.isNullOrBlank()

    val isBusy: Boolean
        get() = isStartingSession || isResuming || isSubmittingCallback
}
