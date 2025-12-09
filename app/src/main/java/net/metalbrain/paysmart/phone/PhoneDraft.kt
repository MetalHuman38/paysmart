package net.metalbrain.paysmart.phone

data class PhoneDraft(
    val e164: String? = null,
    val verificationId: String? = null,
    val verified: Boolean = false
)
