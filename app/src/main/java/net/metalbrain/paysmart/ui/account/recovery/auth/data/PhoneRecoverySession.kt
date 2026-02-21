package net.metalbrain.paysmart.ui.account.recovery.auth.data

data class PhoneRecoverySession(
    val uid: String,
    val phoneNumber: String,
    val idToken: String
)
