package net.metalbrain.paysmart.core.features.identity.config

import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.features.identity.provider.IdentityDocumentTextExtraction
import net.metalbrain.paysmart.core.features.identity.provider.IdentityDocumentTextExtractionMode
import net.metalbrain.paysmart.core.features.identity.provider.IdentityDocumentTextExtractionProvider
import net.metalbrain.paysmart.core.features.identity.provider.IdentityDocumentTextExtractor
import net.metalbrain.paysmart.core.features.identity.provider.OnDeviceIdentityDocumentTextExtractionProvider
import net.metalbrain.paysmart.core.features.identity.provider.RemoteApiIdentityDocumentTextExtractionProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurableIdentityDocumentTextExtractor @Inject constructor(
    private val onDeviceProvider: OnDeviceIdentityDocumentTextExtractionProvider,
    private val remoteProvider: RemoteApiIdentityDocumentTextExtractionProvider
) : IdentityDocumentTextExtractor {

    override suspend fun extract(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityDocumentTextExtraction> {
        val mode = IdentityDocumentTextExtractionMode.from(Env.identityDocumentOcrMode)
        val delegate = when (mode) {
            IdentityDocumentTextExtractionMode.ON_DEVICE -> onDeviceProvider
            IdentityDocumentTextExtractionMode.REMOTE_API -> remoteProvider
        }

        val result = delegate.extract(
            imageBytes = imageBytes,
            mimeType = mimeType
        )
        if (result.isSuccess) return result

        if (!Env.identityDocumentOcrFailOpen || mode == IdentityDocumentTextExtractionMode.ON_DEVICE) {
            return result
        }

        return onDeviceProvider.extract(
            imageBytes = imageBytes,
            mimeType = mimeType
        )
    }
}
