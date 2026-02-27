package net.metalbrain.paysmart.core.features.identity.provider

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteApiIdentityDocumentTextExtractionProvider @Inject constructor(
    private val remoteApi: RemoteIdentityDocumentTextExtractionApi
) : IdentityDocumentTextExtractionProvider {
    override suspend fun extract(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityDocumentTextExtraction> {
        return remoteApi.extract(
            imageBytes = imageBytes,
            mimeType = mimeType
        )
    }
}
