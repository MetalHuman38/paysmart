package net.metalbrain.paysmart.core.features.account.profile.data.type

data class KycDocumentType(
    val id: String,
    val label: String,
    val accepted: Boolean = true
)
