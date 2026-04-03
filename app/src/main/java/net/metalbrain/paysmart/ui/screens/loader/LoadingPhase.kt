package net.metalbrain.paysmart.ui.screens.loader

sealed interface LoadingPhase {
    data object Startup : LoadingPhase
    data object Authentication : LoadingPhase
    data object FetchingData : LoadingPhase
    data object Processing : LoadingPhase
    data object Idle : LoadingPhase
}
