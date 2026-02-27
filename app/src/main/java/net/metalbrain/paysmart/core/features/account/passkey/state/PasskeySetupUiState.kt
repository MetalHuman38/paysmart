package net.metalbrain.paysmart.core.features.account.passkey.state

data class PasskeySetupUiState(
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val statusMessage: String? = null,
    val error: String? = null
)
