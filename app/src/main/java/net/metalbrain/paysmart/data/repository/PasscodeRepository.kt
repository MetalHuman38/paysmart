package net.metalbrain.paysmart.data.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.first
import net.metalbrain.paysmart.domain.crypto.CryptoUseCase
import java.security.MessageDigest
import java.security.SecureRandom

private val Context.dataStore by preferencesDataStore(name = "passcode_store")

@Singleton
class PasscodeRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val cryptoUseCase: CryptoUseCase
) {

    private val dataStore = context.dataStore

    private object Keys {
        val HASH = stringPreferencesKey("pc_hash_v1")
        val SALT = stringPreferencesKey("pc_salt_v1")
        val ITERS = intPreferencesKey("pc_iters_v1")
        val LAST_UNLOCK = longPreferencesKey("pc_last_unlock_v1")
    }

    suspend fun hasPasscode(): Boolean {
        val prefs = dataStore.data.first()
        return prefs.contains(Keys.HASH)
    }

    suspend fun clear() {
        dataStore.edit {
            it.remove(Keys.HASH)
            it.remove(Keys.SALT)
            it.remove(Keys.ITERS)
            it.remove(Keys.LAST_UNLOCK)
        }
    }

    suspend fun setPasscode(passcode: String) {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iterations = 150_000
        val hash = cryptoUseCase.deriveKey(passcode, salt, iterations, 32)

        dataStore.edit { prefs ->
            prefs[Keys.HASH] = Base64.encodeToString(hash, Base64.NO_WRAP)
            prefs[Keys.SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
            prefs[Keys.ITERS] = iterations
        }
    }

    suspend fun verify(passcode: String): Boolean {
        val prefs = dataStore.data.first()

        val hashB64 = prefs[Keys.HASH] ?: return false
        val saltB64 = prefs[Keys.SALT] ?: return false
        val iterations = prefs[Keys.ITERS] ?: 150_000

        val expected = Base64.decode(hashB64, Base64.NO_WRAP)
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val derived = cryptoUseCase.deriveKey(passcode, salt, iterations, 32)


        return MessageDigest.isEqual(expected, derived)
    }

    suspend fun isLockRequired(lockAfterMinutes: Int): Boolean {
        val prefs = dataStore.data.first()
        val lastUnlock = prefs[Keys.LAST_UNLOCK] ?: 0L
        val now = System.currentTimeMillis()
        val timeoutMillis = lockAfterMinutes * 60 * 1000
        return (now - lastUnlock) > timeoutMillis
    }

    suspend fun promptForPasscode(): Boolean {
        return isLockRequired(lockAfterMinutes = 5)
    }

    suspend fun updateLastUnlockTime() {
        dataStore.edit {
            it[Keys.LAST_UNLOCK] = System.currentTimeMillis()
        }
    }
}


//package net.metalbrain.paysmart.data.repository
//
//import android.content.Context
//import android.util.Base64
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.core.intPreferencesKey
//import androidx.datastore.preferences.core.longPreferencesKey
//import androidx.datastore.preferences.core.stringPreferencesKey
//import androidx.datastore.preferences.preferencesDataStore
//import dagger.hilt.android.qualifiers.ApplicationContext
//import jakarta.inject.Inject
//import jakarta.inject.Singleton
//import kotlinx.coroutines.flow.first
//import net.metalbrain.paysmart.domain.crypto.CryptoUseCase
//import java.security.MessageDigest
//import java.security.SecureRandom
//import javax.crypto.SecretKeyFactory
//import javax.crypto.spec.PBEKeySpec
//
//private val Context.dataStore by preferencesDataStore(name = "passcode_store")
//
//@Singleton
//class PasscodeRepository @Inject constructor(
//    @param:ApplicationContext private val context: Context,
//    private val cryptoUseCase: CryptoUseCase
//) {
//
//    private val dataStore = context.dataStore
//
//    private object Keys {
//        val HASH = stringPreferencesKey("pc_hash_v1")
//        val SALT = stringPreferencesKey("pc_salt_v1")
//        val ITERS = intPreferencesKey("pc_iters_v1")
//        val LAST_UNLOCK = longPreferencesKey("pc_last_unlock_v1")
//    }
//
//    suspend fun hasPasscode(): Boolean {
//        val prefs = dataStore.data.first()
//        return prefs.contains(Keys.HASH)
//    }
//
//    suspend fun clear() {
//        dataStore.edit {
//            it.remove(Keys.HASH)
//            it.remove(Keys.SALT)
//            it.remove(Keys.ITERS)
//            it.remove(Keys.LAST_UNLOCK)
//        }
//    }
//
//    suspend fun setPasscode(passcode: String) {
//        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
//        val iterations = 150_000
//        val hash = derive(passcode, salt, iterations)
//
//        dataStore.edit { prefs ->
//            prefs[Keys.HASH] = Base64.encodeToString(hash, Base64.NO_WRAP)
//            prefs[Keys.SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
//            prefs[Keys.ITERS] = iterations
//        }
//    }
//
//    suspend fun verify(passcode: String): Boolean {
//        val prefs = dataStore.data.first()
//
//        val hashB64 = prefs[Keys.HASH] ?: return false
//        val saltB64 = prefs[Keys.SALT] ?: return false
//        val iterations = prefs[Keys.ITERS] ?: 150_000
//
//        val expected = Base64.decode(hashB64, Base64.NO_WRAP)
//        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
//        val derived = derive(passcode, salt, iterations)
//
//        return MessageDigest.isEqual(expected, derived)
//    }
//
//    suspend fun isLockRequired(lockAfterMinutes: Int): Boolean {
//        val prefs = dataStore.data.first()
//        val lastUnlock = prefs[Keys.LAST_UNLOCK] ?: 0L
//        val now = System.currentTimeMillis()
//        val timeoutMillis = lockAfterMinutes * 60 * 1000
//        return (now - lastUnlock) > timeoutMillis
//    }
//
//    suspend fun promptForPasscode(): Boolean {
//        return isLockRequired(lockAfterMinutes = 5)
//    }
//
//    suspend fun updateLastUnlockTime() {
//        dataStore.edit {
//            it[Keys.LAST_UNLOCK] = System.currentTimeMillis()
//        }
//    }
//
//    private fun derive(pass: String, salt: ByteArray, iters: Int, length: Int = 32): ByteArray {
//        val keySpec = PBEKeySpec(pass.toCharArray(), salt, iters, length * 8)
//        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//        return factory.generateSecret(keySpec).encoded
//    }
//}
