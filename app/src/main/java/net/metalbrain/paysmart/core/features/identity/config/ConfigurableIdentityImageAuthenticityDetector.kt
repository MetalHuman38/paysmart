package net.metalbrain.paysmart.core.features.identity.config

import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.features.identity.provider.IdentityImageAuthenticityDetector
import net.metalbrain.paysmart.core.features.identity.provider.IdentityImageDecision
import net.metalbrain.paysmart.core.features.identity.provider.IdentityImageDetectionMode
import net.metalbrain.paysmart.core.features.identity.provider.IdentityImageDetectionResult
import net.metalbrain.paysmart.core.features.identity.provider.OnDeviceIdentityImageDetectionProvider
import net.metalbrain.paysmart.core.features.identity.provider.RemoteAttestationIdentityImageDetectionProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurableIdentityImageAuthenticityDetector @Inject constructor(
    private val onDeviceProvider: OnDeviceIdentityImageDetectionProvider,
    private val remoteProvider: RemoteAttestationIdentityImageDetectionProvider
) : IdentityImageAuthenticityDetector {

    override suspend fun detect(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityImageDetectionResult> {
        val mode = IdentityImageDetectionMode.from(Env.identityImageDetectionMode)
        val delegate = when (mode) {
            IdentityImageDetectionMode.ON_DEVICE -> onDeviceProvider
            IdentityImageDetectionMode.REMOTE_ATTESTATION -> remoteProvider
        }

        val result = delegate.detect(imageBytes, mimeType)
        if (result.isSuccess) return result

        if (!Env.identityImageDetectionFailOpen) {
            return result
        }

        return Result.success(
            IdentityImageDetectionResult(
                decision = IdentityImageDecision.CLEAR,
                confidence = null,
                provider = "fail_open_${mode.name.lowercase()}"
            )
        )
    }
}
