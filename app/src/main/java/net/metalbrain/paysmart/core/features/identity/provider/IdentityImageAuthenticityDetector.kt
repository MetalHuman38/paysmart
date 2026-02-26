package net.metalbrain.paysmart.core.features.identity.provider

enum class IdentityImageDecision {
    CLEAR,
    SUSPECTED_SYNTHETIC
}

data class IdentityImageDetectionResult(
    val decision: IdentityImageDecision,
    val confidence: Float? = null,
    val provider: String
)

enum class IdentityImageDetectionMode {
    ON_DEVICE,
    REMOTE_ATTESTATION;

    companion object {
        fun from(raw: String): IdentityImageDetectionMode {
            return when (raw.trim().lowercase()) {
                "remote_attestation",
                "remote" -> REMOTE_ATTESTATION

                else -> ON_DEVICE
            }
        }
    }
}

interface IdentityImageAuthenticityDetector {
    suspend fun detect(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityImageDetectionResult>
}

interface IdentityImageDetectionProvider {
    suspend fun detect(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityImageDetectionResult>
}

interface RemoteIdentityImageAttestationApi {
    suspend fun attest(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityImageDetectionResult>
}
