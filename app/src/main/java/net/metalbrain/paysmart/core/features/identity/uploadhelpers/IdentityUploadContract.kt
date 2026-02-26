package net.metalbrain.paysmart.core.features.identity.uploadhelpers

enum class IdentityDocumentType {
    PASSPORT,
    DRIVERS_LICENSE,
    NATIONAL_ID
}

enum class IdentityUploadPipelineStage {
    ENCRYPT,
    UPLOAD,
    ATTEST,
    COMMIT
}

data class IdentityUploadSession(
    val sessionId: String,
    val uploadUrl: String,
    val objectPath: String,
    val associatedData: String,
    val attestationNonce: String,
    val encryptionKeyBase64: String,
    val encryptionSchema: String,
    val cryptoContractVersion: String
)

data class EncryptedIdentityPayload(
    val cipherText: ByteArray,
    val plainTextSha256: String,
    val contentType: String,
    val schemaVersion: Int = 1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedIdentityPayload

        if (!cipherText.contentEquals(other.cipherText)) return false
        if (plainTextSha256 != other.plainTextSha256) return false
        if (contentType != other.contentType) return false
        if (schemaVersion != other.schemaVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cipherText.contentHashCode()
        result = 31 * result + plainTextSha256.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + schemaVersion
        return result
    }
}

data class IdentityUploadReceipt(
    val verificationId: String,
    val status: String
)

interface IdentityUploadRepository {
    suspend fun createUploadSession(
        documentType: IdentityDocumentType,
        payloadSha256: String,
        contentType: String
    ): Result<IdentityUploadSession>

    suspend fun uploadEncryptedPayload(
        session: IdentityUploadSession,
        payload: EncryptedIdentityPayload
    ): Result<Unit>

    suspend fun commitUpload(
        sessionId: String,
        payloadSha256: String,
        attestationJwt: String
    ): Result<IdentityUploadReceipt>
}

interface DeviceAttestationProvider {
    suspend fun issueAttestation(nonce: String): Result<String>
}
