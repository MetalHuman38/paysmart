package net.metalbrain.paysmart.data.repository

import android.util.Base64
import android.util.Log
import jakarta.inject.Inject
import net.metalbrain.paysmart.room.doa.AuthSessionLogDao
import net.metalbrain.paysmart.room.entity.AuthSessionLogEntity
import org.json.JSONObject

class AuthSessionLogRepository @Inject constructor(
    private val dao: AuthSessionLogDao
) {
    private companion object {
        const val TAG = "AuthSessionLogRepo"
    }

    suspend fun saveFromIdToken(userId: String, idToken: String): Boolean {
        return try {
            val parsed = parseFromIdToken(userId, idToken)
            if (parsed == null) {
                Log.d(TAG, "Missing sid/sv/ts claims; skipping session log upsert")
                false
            } else {
                dao.upsert(parsed)
                true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist auth session claims", e)
            false
        }
    }

    suspend fun getLatestSessionLog(userId: String): AuthSessionLogEntity? {
        return dao.latestByUserId(userId)
    }

    fun parseFromIdToken(userId: String, idToken: String): AuthSessionLogEntity? {
        val parts = idToken.split(".")
        if (parts.size < 2) {
            return null
        }

        val payloadJson = decodeJwtPayload(parts[1]) ?: return null
        val payload = JSONObject(payloadJson)
        val sid = payload.optString("sid", "").trim()
        if (sid.isBlank()) {
            return null
        }

        val sv = payload.optInt("sv", 1).coerceAtLeast(1)
        val ts = if (payload.has("ts")) payload.optLong("ts", nowSeconds()) else nowSeconds()

        return AuthSessionLogEntity(
            sid = sid,
            userId = userId,
            sessionVersion = sv,
            signInAtSeconds = ts
        )
    }

    private fun decodeJwtPayload(payload: String): String? {
        return try {
            val normalized = payload
                .replace('-', '+')
                .replace('_', '/')
            val padded = normalized.padEnd(((normalized.length + 3) / 4) * 4, '=')
            String(Base64.decode(padded, Base64.DEFAULT), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.w(TAG, "Invalid JWT payload", e)
            null
        }
    }

    private fun nowSeconds(): Long {
        return System.currentTimeMillis() / 1000L
    }
}
