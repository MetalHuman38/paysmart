package net.metalbrain.paysmart.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import androidx.core.content.edit
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64

@Singleton
class PasscodeRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("secure_passcode_store", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_HASH = "pc_hash_v1"
        private const val KEY_SALT = "pc_salt_v1"
        private const val KEY_ITERS = "pc_iters_v1"
    }

    fun hasPasscode(): Boolean = prefs.contains(KEY_HASH)

    fun clear() {
        prefs.edit {
            remove(KEY_HASH)
                .remove(KEY_SALT)
                .remove(KEY_ITERS)
        }
    }

    fun setPasscode(passcode: String) {
        val salt = ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }
        val iterations = 150_000
        val hash = derive(passcode, salt, iterations)

        prefs.edit {
            putString(KEY_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
                .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putInt(KEY_ITERS, iterations)
        }
    }

    fun verify(passcode: String): Boolean {
        val hashB64 = prefs.getString(KEY_HASH, null) ?: return false
        val saltB64 = prefs.getString(KEY_SALT, null) ?: return false
        val iterations = prefs.getInt(KEY_ITERS, 150_000)

        val expected = Base64.decode(hashB64, Base64.NO_WRAP)
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val derived = derive(passcode, salt, iterations)

        return MessageDigest.isEqual(expected, derived) // constant-time
    }

    fun isLockRequired(lockAfterMinutes: Int): Boolean {
        val lastUnlock = prefs.getLong("last_unlock", 0)
        val now = System.currentTimeMillis()
        val timeoutMillis: Int = lockAfterMinutes.times(60).times(1000)

        return (now - lastUnlock) > timeoutMillis
    }

    fun promptForPasscode(): Boolean {
        val lastUnlock = prefs.getLong("last_unlock", 0)
        val now = System.currentTimeMillis()
        val timeoutMillis = 5 * 60 * 1000 // 5 minutes

        return (now - lastUnlock) > timeoutMillis
    }

    private fun derive(pass: String, salt: ByteArray, iters: Int, length: Int = 32): ByteArray {
        val keySpec = PBEKeySpec(pass.toCharArray(), salt, iters, length * 8)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(keySpec).encoded
    }
}
