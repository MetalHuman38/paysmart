package net.metalbrain.paysmart.ui.profile.identity.provider

import javax.inject.Inject
import javax.inject.Singleton

data class IdentityDocumentTextExtraction(
    val fullText: String,
    val candidateFullName: String? = null,
    val provider: String
)

interface IdentityDocumentTextExtractor {
    suspend fun extract(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityDocumentTextExtraction>
}

@Singleton
class OnDeviceIdentityDocumentTextExtractor @Inject constructor() : IdentityDocumentTextExtractor {
    override suspend fun extract(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityDocumentTextExtraction> = runCatching {
        require(imageBytes.isNotEmpty()) { "Document image is empty" }
        require(mimeType.isNotBlank()) { "Document mimeType is empty" }

        // Placeholder: wire ML Kit text recognition / OCR engine here.
        IdentityDocumentTextExtraction(
            fullText = "",
            candidateFullName = null,
            provider = "on_device_ocr_placeholder_v1"
        )
    }
}
