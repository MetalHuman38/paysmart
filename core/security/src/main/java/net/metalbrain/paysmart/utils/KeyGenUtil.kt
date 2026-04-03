package net.metalbrain.paysmart.utils

object KeyGenUtil {
    fun generateHexKey(bytes: Int = 32): String {
        val key = ByteArray(bytes)
        java.security.SecureRandom().nextBytes(key)
        return key.joinToString("") { "%02x".format(it) }
    }
}
