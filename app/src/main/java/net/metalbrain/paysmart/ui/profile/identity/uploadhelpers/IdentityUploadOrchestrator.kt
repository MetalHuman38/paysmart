package net.metalbrain.paysmart.ui.profile.identity.uploadhelpers

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
        onStageChanged: (IdentityUploadPipelineStage) -> Unit = {}
    ): Result<IdentityUploadReceipt> {
        return runCatching {
            require(documentBytes.isNotEmpty()) { "Document payload is empty" }

            onStageChanged(IdentityUploadPipelineStage.ENCRYPT)
            val payloadSha256 = cipher.sha256Base64Url(documentBytes)
            val session = uploadRepository.createUploadSession(
                documentType = documentType,
                payloadSha256 = payloadSha256,
                contentType = contentType
            ).getOrThrow()

            val encrypted = cipher.encrypt(
                plainBytes = documentBytes,
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
