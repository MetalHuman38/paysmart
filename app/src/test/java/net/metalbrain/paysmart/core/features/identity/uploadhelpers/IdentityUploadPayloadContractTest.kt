package net.metalbrain.paysmart.core.features.identity.uploadhelpers

import net.metalbrain.paysmart.core.features.identity.data.ClientInformation
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer

/**
 * Unit tests for [IdentityUploadPayloadContract], ensuring that the encoding process correctly
 * packs the header metadata and the raw document bytes into a single byte array.
 *
 * The tests verify:
 * - Structural integrity of the encoded byte array (header length prefixing).
 * - Correct serialization of the JSON header, including contract versioning and document metadata.
 * - Proper inclusion of the raw document payload.
 */
class IdentityUploadPayloadContractTest {

    @Test
    fun `encode packs header and document bytes`() {
        val payloadBytes = byteArrayOf(10, 20, 30, 40)
        val encoded = IdentityUploadPayloadContract.encode(
            IdentityUploadPayloadInput(
                documentType = IdentityDocumentType.PASSPORT,
                contentType = "image/jpeg",
                documentBytes = payloadBytes,
                clientInformation = ClientInformation(
                    firstName = "Ada",
                    middleName = "Lovelace",
                    lastName = "Byron",
                    email = "ada@example.com",
                    dateOfBirth = "1990-01-01",
                    countryIso2 = "gb"
                ),
                extraction = IdentityUploadExtractionSnapshot(
                    candidateFullName = "Ada Lovelace Byron",
                    provider = "on_device_ocr_placeholder_v1"
                )
            )
        )

        val wrapped = ByteBuffer.wrap(encoded)
        val headerLength = wrapped.int
        val headerBytes = ByteArray(headerLength)
        wrapped.get(headerBytes)
        val documentBytes = ByteArray(wrapped.remaining())
        wrapped.get(documentBytes)

        val headerJson = String(headerBytes, Charsets.UTF_8)
        assertTrue(headerJson.contains("\"contractVersion\":\"$IDENTITY_UPLOAD_PAYLOAD_CONTRACT_VERSION\""))
        assertTrue(headerJson.contains("\"documentType\":\"passport\""))
        assertTrue(headerJson.contains("\"countryIso2\":\"GB\""))
        assertTrue(headerJson.contains("\"provider\":\"on_device_ocr_placeholder_v1\""))
        assertEquals(payloadBytes.size, extractByteLength(headerJson))
        assertArrayEquals(payloadBytes, documentBytes)
    }

    private fun extractByteLength(headerJson: String): Int {
        val marker = "\"byteLength\":"
        val index = headerJson.indexOf(marker)
        assertTrue(index >= 0)
        val tail = headerJson.substring(index + marker.length)
        return tail.takeWhile { it.isDigit() }.toInt()
    }
}
