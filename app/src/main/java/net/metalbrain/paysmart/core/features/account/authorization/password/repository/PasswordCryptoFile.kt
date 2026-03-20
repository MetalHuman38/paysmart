package net.metalbrain.paysmart.core.features.account.authorization.password.repository

import android.content.Context
import androidx.core.content.edit
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeystoreKmsClient
import com.google.crypto.tink.aead.AeadKeyTemplates
import java.io.File
import java.security.KeyStore

class PasswordCryptoFile(
    private val context: Context,
    userId: String,

) {

    private val passwordDirectory = File(context.filesDir, PASSWORD_DIRECTORY).apply {
        mkdirs()
    }
    private val fileName = "$userId.dat"
    private val keysetAlias = "paysmart_password_key_$userId"
    private val prefFile = "paysmart_secure_keys"
    private val legacyFile = File(context.filesDir, "encrypted_password_$userId.dat")

    init {
        // Initialize Tink
        AeadConfig.register()
    }

    private fun aead(): Aead {
        return AndroidKeysetManager.Builder()
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withSharedPref(context, keysetAlias, prefFile)
            .withMasterKeyUri(AndroidKeystoreKmsClient.PREFIX + keysetAlias)
            .build()
            .keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    private val file: File = File(passwordDirectory, fileName)

    private fun activeFileOrNull(): File? {
        return when {
            file.exists() -> file
            legacyFile.exists() -> legacyFile
            else -> null
        }
    }

    fun write(plainText: String) {
        val plainBytes = plainText.toByteArray(Charsets.UTF_8)
        val encrypted = runCatching {
            aead().encrypt(plainBytes, null)
        }.getOrElse {
            clearAll()
            aead().encrypt(plainBytes, null)
        }

        file.parentFile?.mkdirs()
        file.writeBytes(encrypted)
        if (legacyFile.exists()) legacyFile.delete()
    }

    fun read(): String? {
        val activeFile = activeFileOrNull() ?: return null
        val encrypted = runCatching { activeFile.readBytes() }
            .getOrElse {
                clearAll()
                return null
            }

        return runCatching {
            val decrypted = aead().decrypt(encrypted, null)
            decrypted.toString(Charsets.UTF_8)
        }.getOrElse {
            clearAll()
            null
        }
    }

    fun clearAll() {
        try {
            if (file.exists()) file.delete()
            if (legacyFile.exists()) legacyFile.delete()
            context.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
                .edit(commit = true) {
                    remove(keysetAlias)
                }

            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
            keyStore.deleteEntry(keysetAlias)

        } catch (_: Exception) {
        }
    }

    fun exists(): Boolean = activeFileOrNull() != null

    private companion object {
        const val PASSWORD_DIRECTORY = "secure_password"
    }
}
