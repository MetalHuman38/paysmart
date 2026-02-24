package net.metalbrain.paysmart.ui.profile.data.type

data class KycCountryDocuments(
    val iso2: String,
    val documents: List<KycDocumentType>
)
