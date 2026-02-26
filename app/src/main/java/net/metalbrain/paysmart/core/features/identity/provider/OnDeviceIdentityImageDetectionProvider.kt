package net.metalbrain.paysmart.core.features.identity.provider

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceIdentityImageDetectionProvider @Inject constructor() : IdentityImageDetectionProvider {
    override suspend fun detect(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityImageDetectionResult> = runCatching {
        require(imageBytes.isNotEmpty()) { "Captured image is empty" }
        require(mimeType.isNotBlank()) { "Captured image mimeType is empty" }

        // Placeholder hook: replace with on-device ML model inference.
        IdentityImageDetectionResult(
            decision = IdentityImageDecision.CLEAR,
            confidence = null,
            provider = "on_device_placeholder_v1"
        )
    }
}
