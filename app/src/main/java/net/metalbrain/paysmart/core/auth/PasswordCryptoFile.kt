package net.metalbrain.paysmart.core.auth

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeystoreKmsClient
import com.google.crypto.tink.aead.AeadKeyTemplates
import java.io.File

class PasswordCryptoFile(private val context: Context) {

    private val fileName = "encrypted_password.dat"
    private val keysetAlias = "paysmart_password_key"
    private val prefFile = "paysmart_secure_keys"

    init {
        // Initialize Tink
        AeadConfig.register()
    }

    private val aead: Aead by lazy {
        AndroidKeysetManager.Builder()
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withSharedPref(context, keysetAlias, prefFile)
            .withMasterKeyUri(AndroidKeystoreKmsClient.PREFIX + keysetAlias)
            .build()
            .keysetHandle.getPrimitive(Aead::class.java)
    }

    private val file: File = File(context.filesDir, fileName)

    fun write(plainText: String) {
        val encrypted = aead.encrypt(
            plainText.toByteArray(Charsets.UTF_8),
            null // No associated data
        )
        file.writeBytes(encrypted)
    }

    fun read(): String? {
        if (!file.exists()) return null
        val encrypted = file.readBytes()
        return try {
            val decrypted = aead.decrypt(encrypted, null)
            decrypted.toString(Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null // decryption failed (tampering or key mismatch)
        }
    }

    fun clear() {
        if (file.exists()) file.delete()
    }

    fun exists(): Boolean = file.exists()
}
