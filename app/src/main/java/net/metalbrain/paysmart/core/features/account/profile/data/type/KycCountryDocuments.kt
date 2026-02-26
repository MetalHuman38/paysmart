package net.metalbrain.paysmart.core.features.account.profile.data.type

data class KycCountryDocuments(
    val iso2: String,
    val documents: List<KycDocumentType>
)
