package net.metalbrain.paysmart.email

data class EmailLinkUiState(
    val email: String = "",
    val loading: Boolean = false,
    val linkSent: Boolean = false,
    val verified: Boolean = false,
    val error: String? = null
)
