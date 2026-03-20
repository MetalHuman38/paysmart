package net.metalbrain.paysmart.core.features.account.creation.phone.data

data class PhoneDraft(
    val e164: String? = null,
    val verificationId: String? = null,
    val verified: Boolean = false,
    val errorMessage: String? = null
)
