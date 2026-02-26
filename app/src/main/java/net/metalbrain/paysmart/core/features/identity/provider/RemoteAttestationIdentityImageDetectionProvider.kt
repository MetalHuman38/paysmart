package net.metalbrain.paysmart.core.features.identity.provider

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAttestationIdentityImageDetectionProvider @Inject constructor(
    private val remoteApi: RemoteIdentityImageAttestationApi
) : IdentityImageDetectionProvider {
    override suspend fun detect(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityImageDetectionResult> {
        return remoteApi.attest(
            imageBytes = imageBytes,
            mimeType = mimeType
        )
    }
}
