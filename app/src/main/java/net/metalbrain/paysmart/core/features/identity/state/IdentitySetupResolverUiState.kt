package net.metalbrain.paysmart.core.features.identity.state

import net.metalbrain.paysmart.core.features.account.profile.data.type.KycDocumentType
import net.metalbrain.paysmart.core.features.account.profile.data.type.KycReviewWindow
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityDocumentType
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityUploadReceipt
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityResolverStep
import java.util.Locale

data class IdentitySetupResolverUiState(
    val selectedCountryIso2: String = "GB",
    val selectedCountryReviewWindow: KycReviewWindow = KycReviewWindow(),
    val availableCountriesIso2: List<String> = emptyList(),
    val availableDocuments: List<KycDocumentType> = emptyList(),
    val selectedDocumentId: String = "",
    val selectedDocumentName: String? = null,
    val selectedMimeType: String? = null,
    val selectedDocumentSizeBytes: Int = 0,
    val hasCapturedDocument: Boolean = false,
    val currentStep: IdentityResolverStep = IdentityResolverStep.CAPTURE,
    val isValidatingCapture: Boolean = false,
    val isProcessing: Boolean = false,
    val failedStep: IdentityResolverStep? = null,
    val receipt: IdentityUploadReceipt? = null,
    val nameMatchWarning: String? = null,
    val error: String? = null
) {
    val selectedDocument: KycDocumentType?
        get() = availableDocuments.firstOrNull { it.id == selectedDocumentId }

    val isSelectedDocumentAccepted: Boolean
        get() = selectedDocument?.accepted == true

    val isSelectedDocumentUploadSupported: Boolean
        get() = selectedDocument?.toUploadDocumentType() != null

    val hasSubmittedForReview: Boolean
        get() = receipt != null && currentStep == IdentityResolverStep.COMPLETE
}

private fun KycDocumentType.toUploadDocumentType(): IdentityDocumentType? {
    return when (id.lowercase(Locale.US)) {
        "passport" -> IdentityDocumentType.PASSPORT
        "drivers_license" -> IdentityDocumentType.DRIVERS_LICENSE
        "national_id" -> IdentityDocumentType.NATIONAL_ID
        else -> null
    }
}
