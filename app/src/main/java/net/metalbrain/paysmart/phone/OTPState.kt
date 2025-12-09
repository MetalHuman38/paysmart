package net.metalbrain.paysmart.phone

data class OTPState(
    val code: String = "",
    val remainingSeconds: Int = 0,
    val isResendAvailable: Boolean = false,
    val verificationId: String? = null
)
