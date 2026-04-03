package net.metalbrain.paysmart.core.features.account.authentication.email.state

data class EmailLinkUiState(
    val email: String = "",
    val loading: Boolean = false,
    val linkSent: Boolean = false,
    val verified: Boolean = false,
    val error: String? = null
)
