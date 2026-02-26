package net.metalbrain.paysmart.core.features.identity.uploadhelpers

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64 as JBase64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class IdentityUploadCipher @Inject constructor() {
    companion object {
        private const val ENCRYPTION_SCHEMA = "aes-256-gcm-v1"
        private const val SCHEMA_VERSION: Byte = 1
        private const val KEY_BYTES = 32
        private const val IV_BYTES = 12
        private const val TAG_BITS = 128
    }

    fun encrypt(
        plainBytes: ByteArray,
        associatedData: ByteArray,
        contentType: String,
        encryptionKeyBase64: String,
        encryptionSchema: String
    ): EncryptedIdentityPayload {
        require(encryptionSchema == ENCRYPTION_SCHEMA) { "Unsupported encryption schema: $encryptionSchema" }
        val keyBytes = decodeKey(encryptionKeyBase64)
        require(keyBytes.size == KEY_BYTES) { "Invalid encryption key length: ${keyBytes.size}" }

        val iv = ByteArray(IV_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val gcmSpec = GCMParameterSpec(TAG_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        cipher.updateAAD(associatedData)
        val cipherAndTag = cipher.doFinal(plainBytes)

        val packedCipherText = ByteArray(1 + iv.size + cipherAndTag.size)
        packedCipherText[0] = SCHEMA_VERSION
        System.arraycopy(iv, 0, packedCipherText, 1, iv.size)
        System.arraycopy(cipherAndTag, 0, packedCipherText, 1 + iv.size, cipherAndTag.size)

        return EncryptedIdentityPayload(
            cipherText = packedCipherText,
            plainTextSha256 = sha256Base64Url(plainBytes),
            contentType = contentType,
            schemaVersion = SCHEMA_VERSION.toInt()
        )
    }

    fun decrypt(
        cipherText: ByteArray,
        associatedData: ByteArray,
        encryptionKeyBase64: String,
        encryptionSchema: String
    ): ByteArray {
        require(encryptionSchema == ENCRYPTION_SCHEMA) { "Unsupported encryption schema: $encryptionSchema" }
        val keyBytes = decodeKey(encryptionKeyBase64)
        require(keyBytes.size == KEY_BYTES) { "Invalid encryption key length: ${keyBytes.size}" }
        require(cipherText.size > 1 + IV_BYTES) { "Encrypted payload too small" }
        require(cipherText[0] == SCHEMA_VERSION) { "Unsupported payload schema version: ${cipherText[0]}" }

        val iv = cipherText.copyOfRange(1, 1 + IV_BYTES)
        val encryptedBody = cipherText.copyOfRange(1 + IV_BYTES, cipherText.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val gcmSpec = GCMParameterSpec(TAG_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        cipher.updateAAD(associatedData)
        return cipher.doFinal(encryptedBody)
    }

    fun sha256Base64Url(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return Base64.encodeToString(
            digest,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
    }

    private fun decodeKey(base64Value: String): ByteArray {
        val normalized = base64Value.trim()
        require(normalized.isNotBlank()) { "Missing encryption key" }

        return runCatching {
            JBase64.getDecoder().decode(normalized)
        }.recoverCatching {
            JBase64.getUrlDecoder().decode(normalized)
        }.getOrElse {
            throw IllegalArgumentException("Invalid encryption key encoding")
        }
    }
}
