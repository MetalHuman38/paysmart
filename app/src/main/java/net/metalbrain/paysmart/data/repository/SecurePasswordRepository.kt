package net.metalbrain.paysmart.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import net.metalbrain.paysmart.core.auth.BcryptPasswordHasher
import net.metalbrain.paysmart.core.auth.PasswordCryptoFile

class SecurePasswordRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val hasher: BcryptPasswordHasher
) : PasswordRepository {

    private val file = PasswordCryptoFile(context)

    override suspend fun setPassword(plain: String) {
        if (file.exists()) {
            val currentHash = file.read() ?: throw IllegalStateException("File exists but unreadable")
            if (!hasher.verify(plain, currentHash)) {
                throw IllegalStateException("Password mismatch")
            }
            if (hasher.needsRehash(currentHash)) {
                val newHash = hasher.hash(plain)
                file.write(newHash)
            }
        } else {
            val newHash = hasher.hash(plain)
            file.write(newHash)
        }
    }

    override suspend fun verify(plain: String): Boolean {
        val stored = file.read() ?: return false
        val ok = hasher.verify(plain, stored)
        if (ok && hasher.needsRehash(stored)) {
            val newHash = hasher.hash(plain)
            file.write(newHash)
        }
        return ok
    }

    override suspend fun hasPassword(): Boolean {
        return file.exists()
    }

    override suspend fun changePassword(old: String, new: String): Boolean {
        val stored = file.read() ?: return false

        if (!hasher.verify(old, stored)) return false

        val newHash = hasher.hash(new)
        file.write(newHash)

        return true
    }

    override suspend fun clear() = file.clear()
}
