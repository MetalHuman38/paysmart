package net.metalbrain.paysmart.core.features.account.profile.data.type

data class KycReviewWindow(
    val minHours: Int = 24,
    val maxHours: Int = 48
)

data class KycCountryDocuments(
    val iso2: String,
    val documents: List<KycDocumentType>,
    val reviewWindow: KycReviewWindow = KycReviewWindow()
)
