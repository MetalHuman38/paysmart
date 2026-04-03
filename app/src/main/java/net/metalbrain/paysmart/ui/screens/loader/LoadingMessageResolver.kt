package net.metalbrain.paysmart.ui.screens.loader

data class LoadingMessageSpec(
    val message: String,
    val hint: String? = null,
)

fun resolveMessage(phase: LoadingPhase): LoadingMessageSpec {
    return when (phase) {
        LoadingPhase.Startup -> LoadingMessageSpec(
            message = "Starting up...",
            hint = "Preparing PaySmart securely."
        )

        LoadingPhase.Authentication -> LoadingMessageSpec(
            message = "Signing you in...",
            hint = "Checking your account and security state."
        )

        LoadingPhase.FetchingData -> LoadingMessageSpec(
            message = "Preparing your workspace...",
            hint = "Loading the details you need next."
        )

        LoadingPhase.Processing -> LoadingMessageSpec(
            message = "Finalizing...",
            hint = "Completing the last secure step."
        )

        LoadingPhase.Idle -> LoadingMessageSpec(message = "")
    }
}
