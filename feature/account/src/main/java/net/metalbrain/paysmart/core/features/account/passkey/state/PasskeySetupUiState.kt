package net.metalbrain.paysmart.core.features.account.passkey.state

import net.metalbrain.paysmart.core.features.account.passkey.repository.PasskeyCredentialItem

data class PasskeySetupUiState(
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val credentials: List<PasskeyCredentialItem> = emptyList(),
    val activeRevokeCredentialId: String? = null,
    val statusMessage: String? = null,
    val error: String? = null
)
