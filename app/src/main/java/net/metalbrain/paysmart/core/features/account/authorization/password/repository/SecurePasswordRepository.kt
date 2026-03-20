package net.metalbrain.paysmart.core.features.account.authorization.password.repository

import android.content.Context
import com.google.firebase.Timestamp
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
import net.metalbrain.paysmart.domain.auth.UserManager

class SecurePasswordRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val hasher: BcryptPasswordHasher,
    private val securityPreference: SecurityPreference,
    private val userManager: UserManager,
    private val passwordPolicyHandler: PasswordPolicyHandler
) : PasswordRepository {

    /**
     * Always resolved at call time — never at injection time.
     *
     * [UserManager.uid] throws if called before the user is authenticated.
     * Dagger constructs this repository eagerly, so binding the file to a
     * property initializer would crash on every cold start before sign-in.
     */
    private fun currentFile(): PasswordCryptoFile =
        PasswordCryptoFile(context, userManager.uid)

    override suspend fun setPassword(plain: String, idToken: String) {
        val file = currentFile()
        val previousState = securityPreference.loadLocalSecurityState()

        var previousHash: String? = null
        if (file.exists()) {
            val currentHash = file.read()
            if (currentHash == null) {
                resetCorruptedPasswordState(file)
            } else {
                previousHash = currentHash
                if (!hasher.verify(plain, currentHash)) {
                    throw IllegalStateException("Password mismatch")
                }
                if (hasher.needsRehash(currentHash)) {
                    file.write(hasher.hash(plain))
                }
                return
            }
        }

        val nextHash = hasher.hash(plain)
        try {
            file.write(nextHash)
        } catch (e: Exception) {
            resetCorruptedPasswordState(file)
            throw IllegalStateException("Unable to initialize secure password storage", e)
        }

        val serverAccepted = passwordPolicyHandler.setPasswordEnabled(idToken)
        if (!serverAccepted) {
            restorePasswordState(file = file, previousHash = previousHash, previousState = previousState)
            throw IllegalStateException("Server failed to acknowledge password enablement")
        }

        securityPreference.saveLocalSecurityState(
            previousState.copy(
                passwordEnabled = true,
                localPasswordSetAt = Timestamp.now()
            )
        )
    }

    override suspend fun verify(plain: String): Boolean {
        val file = currentFile()
        val stored = file.read()
        if (stored == null) {
            resetCorruptedPasswordState(file)
            return false
        }
        val ok = hasher.verify(plain, stored)
        if (ok && hasher.needsRehash(stored)) {
            file.write(hasher.hash(plain))
        }
        return ok
    }

    override suspend fun hasPassword(): Boolean {
        val file = currentFile()
        val stored = file.read()
        if (stored == null) {
            resetCorruptedPasswordState(file)
            return false
        }
        return true
    }

    override suspend fun changePassword(old: String, new: String, idToken: String): Boolean {
        val file = currentFile()
        val stored = file.read()
        if (stored == null) {
            resetCorruptedPasswordState(file)
            return false
        }
        if (!hasher.verify(old, stored)) return false

        val previousState = securityPreference.loadLocalSecurityState()
        try {
            file.write(hasher.hash(new))
        } catch (e: Exception) {
            resetCorruptedPasswordState(file)
            throw IllegalStateException("Unable to update secure password storage", e)
        }

        return try {
            val serverAccepted = passwordPolicyHandler.setPasswordEnabled(idToken)
            if (!serverAccepted) {
                restorePasswordState(file = file, previousHash = stored, previousState = previousState)
                return false
            }
            securityPreference.saveLocalSecurityState(
                previousState.copy(
                    passwordEnabled = true,
                    localPasswordSetAt = Timestamp.now()
                )
            )
            true
        } catch (e: Exception) {
            restorePasswordState(file = file, previousHash = stored, previousState = previousState)
            throw e
        }
    }

    override suspend fun clear() {
        resetCorruptedPasswordState(currentFile())
    }

    private suspend fun resetCorruptedPasswordState(file: PasswordCryptoFile) {
        file.clearAll()
        val currentState = securityPreference.loadLocalSecurityState()
        securityPreference.saveLocalSecurityState(
            currentState.copy(
                passwordEnabled = false,
                localPasswordSetAt = null
            )
        )
    }

    private suspend fun restorePasswordState(
        file: PasswordCryptoFile,
        previousHash: String?,
        previousState: net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
    ) {
        runCatching {
            if (previousHash == null) {
                file.clearAll()
            } else {
                file.write(previousHash)
            }
        }.onFailure {
            file.clearAll()
        }
        securityPreference.saveLocalSecurityState(previousState)
    }
}
//package net.metalbrain.paysmart.core.features.account.authorization.password.repository
//
//import android.content.Context
//import com.google.firebase.Timestamp
//import dagger.hilt.android.qualifiers.ApplicationContext
//import jakarta.inject.Inject
//import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
//import net.metalbrain.paysmart.domain.auth.UserManager
//
//class SecurePasswordRepository @Inject constructor(
//    @param:ApplicationContext private val context: Context,
//    private val hasher: BcryptPasswordHasher,
//    private val securityPreference: SecurityPreference,
//    private val userManager: UserManager,
//    private val passwordPolicyHandler: PasswordPolicyHandler
//) : PasswordRepository {
//
//    private fun currentFile(): CryptoFile =
//        CryptoFile(context, userManager.uid)
//
//    private val file = currentFile()
//
//
//    override suspend fun setPassword(plain: String, idToken: String) {
//        if (file.exists()) {
//            val currentHash = file.read()
//
//            if (currentHash == null) {
//                // 🔥 KEYSTORE CORRUPTION → RESET
//                file.clearAll()
//            } else {
//                if (!hasher.verify(plain, currentHash)) {
//                    throw IllegalStateException("Password mismatch")
//                }
//                if (hasher.needsRehash(currentHash)) {
//                    val newHash = hasher.hash(plain)
//                    file.write(newHash)
//                }
//                return
//            }
//        }
//
//        val serverAccepted = passwordPolicyHandler.setPasswordEnabled(idToken)
//        val updated = securityPreference
//            .loadLocalSecurityState()
//            .copy(
//                passwordEnabled = serverAccepted,
//                localPasswordSetAt = if (serverAccepted) Timestamp.now() else null
//            )
//        securityPreference.saveLocalSecurityState(updated)
//
//        if (!serverAccepted) {
//            throw IllegalStateException("Server failed to acknowledge password enablement")
//        }
//    }
//
//    override suspend fun verify(plain: String): Boolean {
//        val stored = file.read()
//        if (stored == null) {
//            file.clearAll() // reset
//            return false
//        }
//        val ok = hasher.verify(plain, stored)
//        if (ok && hasher.needsRehash(stored)) {
//            val newHash = hasher.hash(plain)
//            file.write(newHash)
//        }
//        return ok
//    }
//
//    override suspend fun hasPassword(): Boolean {
//        return file.read() != null
//    }
//
//    override suspend fun changePassword(old: String, new: String, idToken: String): Boolean {
//        val stored = file.read() ?: return false
//        if (!hasher.verify(old, stored)) return false
//
//        val previousState = securityPreference.loadLocalSecurityState()
//        val nextHash = hasher.hash(new)
//        file.write(nextHash)
//
//        return try {
//            val serverAccepted = passwordPolicyHandler.setPasswordEnabled(idToken)
//            if (!serverAccepted) {
//                file.write(stored)
//                securityPreference.saveLocalSecurityState(previousState)
//                return false
//            }
//
//            securityPreference.saveLocalSecurityState(
//                previousState.copy(
//                    passwordEnabled = true,
//                    localPasswordSetAt = Timestamp.now()
//                )
//            )
//            true
//        } catch (e: Exception) {
//            file.write(stored)
//            securityPreference.saveLocalSecurityState(previousState)
//            throw e
//        }
//    }
//
//    override suspend fun clear() = file.clearAll()
//}
