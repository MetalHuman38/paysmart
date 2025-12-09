package net.metalbrain.paysmart.core.auth

import android.content.Context
import androidx.security.crypto.EncryptedFile.Builder
import androidx.security.crypto.EncryptedFile.FileEncryptionScheme
import androidx.security.crypto.MasterKey
import java.io.File

class PasswordCryptoFile(private val context: Context) {

    private val fileName = "encrypted_password.dat"

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val file: File = File(context.filesDir, fileName)

    private val encryptedFile = Builder(
        context,
        file,
        masterKey,
        FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()

    fun write(content: String) {
        encryptedFile.openFileOutput().use {
            it.write(content.toByteArray(Charsets.UTF_8))
        }
    }

    fun read(): String? {
        if (!file.exists()) return null
        return encryptedFile.openFileInput().bufferedReader().use { it.readText() }
    }

    fun clear() {
        if (file.exists()) file.delete()
    }

    fun exists(): Boolean = file.exists()
}
