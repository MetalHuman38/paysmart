package net.metalbrain.paysmart.data.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.domain.room.NativeRoomEncryptor
import net.metalbrain.paysmart.utils.ThrottledLogger
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

private val Context.roomKeyStore by preferencesDataStore(name = "room_passphrase_store")

@Singleton
class RoomPassphraseRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val encryptor: NativeRoomEncryptor,
    private val securityPreference: SecurityPreference
) {

    private val store = context.roomKeyStore

    private object Keys {
        val SALT = stringPreferencesKey("room_key_salt")
        val KEY = stringPreferencesKey("room_derived_key")
    }

    private val lockedLogger = ThrottledLogger(
        tag = "RoomKeyAccess",
        minIntervalMs = 60_000L // log at most once every 10 seconds
    )

    suspend fun getRoomKey(): ByteArray {
        val prefs = store.data.first()

        val existingKey = prefs[Keys.KEY]
        if (existingKey != null) {
            return Base64.decode(existingKey, Base64.NO_WRAP)
        }

        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val passphrase = generateRandomPassphrase()
        val derivedKey = encryptor.deriveKey(passphrase, salt)

        store.edit {
            it[Keys.SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
            it[Keys.KEY] = Base64.encodeToString(derivedKey, Base64.NO_WRAP)
        }

        return derivedKey
    }

    private fun generateRandomPassphrase(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    suspend fun clearKey() {
        store.edit { it.clear() }
    }
}
