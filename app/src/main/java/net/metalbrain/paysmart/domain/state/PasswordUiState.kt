package net.metalbrain.paysmart.domain.state

data class PasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val showPassword: Boolean = false,
    val loading: Boolean = false
)
