// net.metalbrain.paysmart.core.security.SecureKeyAlias.kt

package net.metalbrain.paysmart.core.security

import java.security.MessageDigest

/**
 * Derives an opaque, fixed-length alias from a raw user ID.
 *
 * The raw Firebase UID must never appear in file names, SharedPrefs keys,
 * or Keystore aliases — those surfaces can be observed in backups or bug
 * reports. AN SHA-256 truncated to 16 bytes (32 hex chars) is collision-resistant at this scale and leaks nothing about the original value.
 */
object SecureKeyAlias {

    fun from(uid: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest
            .digest(uid.toByteArray(Charsets.UTF_8))
            .take(16)
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
