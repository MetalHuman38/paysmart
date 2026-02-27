package net.metalbrain.paysmart.core.features.identity.uploadhelpers

import net.metalbrain.paysmart.core.features.identity.data.ClientInformation
import java.nio.ByteBuffer
import java.util.Locale

private const val HEADER_LENGTH_BYTES = 4
const val IDENTITY_UPLOAD_PAYLOAD_CONTRACT_VERSION = "identity-upload-v2"

data class IdentityUploadExtractionSnapshot(
    val candidateFullName: String?,
    val provider: String
)

data class IdentityUploadPayloadInput(
    val documentType: IdentityDocumentType,
    val contentType: String,
    val documentBytes: ByteArray,
    val clientInformation: ClientInformation,
    val extraction: IdentityUploadExtractionSnapshot?
)

object IdentityUploadPayloadContract {
    fun encode(input: IdentityUploadPayloadInput): ByteArray {
        require(input.documentBytes.isNotEmpty()) { "Document payload is empty" }
        val headerBytes = buildHeaderJson(input).toByteArray(Charsets.UTF_8)
        val buffer = ByteBuffer.allocate(
            HEADER_LENGTH_BYTES + headerBytes.size + input.documentBytes.size
        )
        buffer.putInt(headerBytes.size)
        buffer.put(headerBytes)
        buffer.put(input.documentBytes)
        return buffer.array()
    }

    private fun buildHeaderJson(input: IdentityUploadPayloadInput): String {
        val client = input.clientInformation
        val extraction = input.extraction
        return buildString {
            append("{")
            append("\"contractVersion\":\"")
            append(escape(IDENTITY_UPLOAD_PAYLOAD_CONTRACT_VERSION))
            append("\",")
            append("\"document\":{")
            append("\"documentType\":\"")
            append(escape(input.documentType.toApiValue()))
            append("\",")
            append("\"contentType\":\"")
            append(escape(input.contentType.trim()))
            append("\",")
            append("\"byteLength\":")
            append(input.documentBytes.size)
            append("},")
            append("\"clientInfo\":{")
            append("\"firstName\":\"")
            append(escape(client.firstName.trim()))
            append("\",")
            append("\"middleName\":\"")
            append(escape(client.middleName?.trim().orEmpty()))
            append("\",")
            append("\"lastName\":\"")
            append(escape(client.lastName.trim()))
            append("\",")
            append("\"fullName\":\"")
            append(escape(client.fullName.trim()))
            append("\",")
            append("\"email\":\"")
            append(escape(client.email.trim()))
            append("\",")
            append("\"dateOfBirth\":\"")
            append(escape(client.dateOfBirth.trim()))
            append("\",")
            append("\"countryIso2\":\"")
            append(escape(client.countryIso2.trim().uppercase(Locale.US)))
            append("\"},")
            append("\"extraction\":{")
            append("\"candidateFullName\":\"")
            append(escape(extraction?.candidateFullName?.trim().orEmpty()))
            append("\",")
            append("\"provider\":\"")
            append(escape(extraction?.provider?.trim().orEmpty()))
            append("\"}")
            append("}")
        }
    }

    private fun IdentityDocumentType.toApiValue(): String {
        return when (this) {
            IdentityDocumentType.PASSPORT -> "passport"
            IdentityDocumentType.DRIVERS_LICENSE -> "drivers_license"
            IdentityDocumentType.NATIONAL_ID -> "national_id"
        }
    }

    private fun escape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
