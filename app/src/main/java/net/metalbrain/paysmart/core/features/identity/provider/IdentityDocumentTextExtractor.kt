package net.metalbrain.paysmart.core.features.identity.provider

import javax.inject.Inject
import javax.inject.Singleton

data class IdentityDocumentTextExtraction(
    val fullText: String,
    val candidateFullName: String? = null,
    val provider: String
)

enum class IdentityDocumentTextExtractionMode {
    ON_DEVICE,
    REMOTE_API;

    companion object {
        fun from(raw: String): IdentityDocumentTextExtractionMode {
            return when (raw.trim().lowercase()) {
                "remote_api",
                "remote" -> REMOTE_API
                else -> ON_DEVICE
            }
        }
    }
}

interface IdentityDocumentTextExtractor {
    suspend fun extract(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityDocumentTextExtraction>
}

interface IdentityDocumentTextExtractionProvider {
    suspend fun extract(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityDocumentTextExtraction>
}

interface RemoteIdentityDocumentTextExtractionApi {
    suspend fun extract(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityDocumentTextExtraction>
}

@Singleton
class OnDeviceIdentityDocumentTextExtractionProvider @Inject constructor() :
    IdentityDocumentTextExtractionProvider {
    override suspend fun extract(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityDocumentTextExtraction> = runCatching {
        require(imageBytes.isNotEmpty()) { "Document image is empty" }
        require(mimeType.isNotBlank()) { "Document mimeType is empty" }

        // Fallback placeholder for offline/dev mode.
        IdentityDocumentTextExtraction(
            fullText = "",
            candidateFullName = null,
            provider = "on_device_ocr_placeholder_v1"
        )
    }
}
