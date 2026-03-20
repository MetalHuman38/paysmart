package net.metalbrain.paysmart.core.features.account.authorization.password.repository

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeystoreKmsClient
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import net.metalbrain.paysmart.core.security.SecureKeyAlias
import java.io.File
import java.security.KeyStore
import androidx.core.content.edit

class CryptoFile(
    private val context: Context,
    userId: String
) {
    // Opaque alias — the raw UID never touches disk or SharedPrefs.
    private val alias = SecureKeyAlias.from(userId)

    private val fileName = "ep_$alias.dat"
    private val keysetAlias = "psk_$alias"
    private val prefFile = "paysmart_secure_keys"

    init {
        AeadConfig.register()
    }

    /**
     * Builds the AEAD primitive, recovering automatically if the Keystore
     * master key was wiped (uninstall + reinstall with backup restore).
     *
     * Flow:
     *  1. Try to build normally — succeeds on a clean install or a live session.
     *  2. If it throws, the most likely cause is a stale Tink keyset in
     *     SharedPrefs whose wrapping key no longer exists in the Keystore.
     *     Wipe both and rebuild fresh so the repository stays functional.
     */
    private fun buildAead(): Aead {
        return try {
            createAead()
        } catch (e: Exception) {
            // Master key is gone (uninstall/reinstall). Purge the stale
            // keyset so the next call creates a fresh one.
            wipeKeysetFromPrefs()
            createAead()
        }
    }

    private fun createAead(): Aead =
        AndroidKeysetManager.Builder()
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withSharedPref(context, keysetAlias, prefFile)
            .withMasterKeyUri(AndroidKeystoreKmsClient.PREFIX + keysetAlias)
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)

    // Lazily initialized but re-entrant-safe: Kotlin `lazy` is
    // SYNCHRONIZED by default, so concurrent first-access is safe.
    private val aead: Aead by lazy { buildAead() }

    private val file: File
        get() = File(context.filesDir, fileName)

    // ── Public API ────────────────────────────────────────────────────────

    fun write(plainText: String) {
        val encrypted = aead.encrypt(plainText.toByteArray(Charsets.UTF_8), null)
        file.writeBytes(encrypted)
    }

    fun read(): String? {
        if (!file.exists()) return null
        return try {
            val decrypted = aead.decrypt(file.readBytes(), null)
            decrypted.toString(Charsets.UTF_8)
        } catch (e: Exception) {
            // Decryption failed — treat as corrupted.
            null
        }
    }

    fun exists(): Boolean = file.exists()

    /**
     * Full wipe: encrypted file + Keystore entry + SharedPrefs keyset.
     *
     * Previously only the Keystore entry was removed, leaving a stale
     * Tink keyset in SharedPrefs. On the next cold start [buildAead]
     * would find the orphaned keyset and fail to unwrap it.
     */
    fun clearAll() {
        runCatching { if (file.exists()) file.delete() }
        wipeKeysetFromPrefs()
        wipeKeystoreEntry()
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private fun wipeKeysetFromPrefs() {
        runCatching {
            context
                .getSharedPreferences(prefFile, Context.MODE_PRIVATE)
                .edit {
                    remove(keysetAlias)
                }
        }
    }

    private fun wipeKeystoreEntry() {
        runCatching {
            KeyStore.getInstance("AndroidKeyStore")
                .apply { load(null) }
                .deleteEntry(keysetAlias)
        }
    }
}
