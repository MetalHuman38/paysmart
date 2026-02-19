package net.metalbrain.paysmart.domain.room

import android.util.Base64
import net.metalbrain.paysmart.data.native.RoomNativeBridge
import java.security.SecureRandom
import javax.inject.Inject

class NativeRoomEncryptor @Inject constructor() {

    companion object {
        private const val SALT_LENGTH = 16
        private const val ITERATIONS = 150_000
        private const val KEY_LENGTH = 32
    }

    private fun generateSalt(): ByteArray =
        ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }

    fun deriveKey(passphrase: String, salt: ByteArray): ByteArray =
        RoomNativeBridge.deriveRoomKey(passphrase, salt, ITERATIONS, KEY_LENGTH)

    fun encode(b: ByteArray): String =
        Base64.encodeToString(b, Base64.NO_WRAP)

    fun decode(s: String): ByteArray =
        Base64.decode(s, Base64.NO_WRAP)
}
