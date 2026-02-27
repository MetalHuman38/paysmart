package net.metalbrain.paysmart.core.features.identity.uploadhelpers

import net.metalbrain.paysmart.core.features.identity.data.ClientInformation
import javax.inject.Inject

class IdentityUploadOrchestrator @Inject constructor(
    private val uploadRepository: IdentityUploadRepository,
    private val attestationProvider: DeviceAttestationProvider,
    private val cipher: IdentityUploadCipher
) {
    suspend fun uploadDocument(
        documentType: IdentityDocumentType,
        documentBytes: ByteArray,
        contentType: String,
        clientInformation: ClientInformation,
        extraction: IdentityUploadExtractionSnapshot? = null,
        onStageChanged: (IdentityUploadPipelineStage) -> Unit = {}
    ): Result<IdentityUploadReceipt> {
        return runCatching {
            require(documentBytes.isNotEmpty()) { "Document payload is empty" }

            onStageChanged(IdentityUploadPipelineStage.ENCRYPT)
            val plainPayload = IdentityUploadPayloadContract.encode(
                IdentityUploadPayloadInput(
                    documentType = documentType,
                    contentType = contentType,
                    documentBytes = documentBytes,
                    clientInformation = clientInformation,
                    extraction = extraction
                )
            )
            val payloadSha256 = cipher.sha256Base64Url(plainPayload)
            val session = uploadRepository.createUploadSession(
                documentType = documentType,
                payloadSha256 = payloadSha256,
                contentType = contentType
            ).getOrThrow()

            val encrypted = cipher.encrypt(
                plainBytes = plainPayload,
                associatedData = session.associatedData.toByteArray(Charsets.UTF_8),
                contentType = contentType,
                encryptionKeyBase64 = session.encryptionKeyBase64,
                encryptionSchema = session.encryptionSchema
            )

            onStageChanged(IdentityUploadPipelineStage.UPLOAD)
            uploadRepository.uploadEncryptedPayload(
                session = session,
                payload = encrypted
            ).getOrThrow()

            onStageChanged(IdentityUploadPipelineStage.ATTEST)
            val attestationJwt = attestationProvider.issueAttestation(
                session.attestationNonce
            ).getOrThrow()

            onStageChanged(IdentityUploadPipelineStage.COMMIT)
            uploadRepository.commitUpload(
                sessionId = session.sessionId,
                payloadSha256 = encrypted.plainTextSha256,
                attestationJwt = attestationJwt
            ).getOrThrow()
        }
    }
}
