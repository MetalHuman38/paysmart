package net.metalbrain.paysmart.core.security

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.core.auth.SecuritySettingsHandler
import net.metalbrain.paysmart.data.repository.AuthSessionLogRepository
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import net.metalbrain.paysmart.domain.room.RoomUseCase
import javax.inject.Inject

class SecuritySyncManager @Inject constructor(
    private val securityPreference: SecurityPreference,
    private val securityPolicyHandler: SecuritySettingsHandler,
    private val roomUseCase: RoomUseCase,
    private val securityParity: SecurityParity,
    private val authSessionLogRepository: AuthSessionLogRepository,
) {
    private companion object {
        const val TAG = "SecuritySync"
    }

    private val syncMutex = Mutex()

    private fun logParity(
        source: String,
        server: SecuritySettingsModel?,
        room: SecuritySettingsModel?,
        localMirror: LocalSecuritySettingsModel?
    ) {
        Log.d(
            TAG,
            "parity[$source] server={${securityParity.signatureFromServer(server)}} room={${securityParity.signatureFromServer(room)}} local={${securityParity.signatureFromLocal(localMirror)}}"
        )

        if (server != null) {
            val roomParity = securityParity.assertServerRoomParity(server, room)
            if (roomParity.matches) {
                Log.d(TAG, "parity_assertion_ok[$source:server_room]")
            } else {
                Log.w(
                    TAG,
                    "parity_assertion_failed[$source:server_room] mismatches=${securityParity.mismatchLabels("room", roomParity.mismatches)}"
                )
            }
        }

        if (localMirror != null && room != null) {
            val localParity = securityParity.assertRoomLocalParity(
                room = room,
                local = localMirror,
                ignoreFields = securityParity.STICKY_LOCAL_FIELDS
            )
            if (localParity.matches) {
                Log.d(TAG, "parity_assertion_ok[$source:room_local]")
            } else {
                Log.w(
                    TAG,
                    "parity_assertion_failed[$source:room_local] mismatches=${securityParity.mismatchLabels("local", localParity.mismatches)}"
                )
            }
        }
    }

    private suspend fun mirrorToDataStoreIfEnabled(
        sourceModel: SecuritySettingsModel
    ): LocalSecuritySettingsModel? {
        if (!SecurityMigrationFlags.legacyDatastoreMirrorEnabled) {
            return null
        }

        val localState = securityPreference.loadLocalSecurityState()
        val merged = securityPreference.mergeServerWithLocal(sourceModel, localState)
        securityPreference.saveLocalSecurityState(merged)
        securityPreference.saveCloudSecuritySettings(sourceModel)
        return merged
    }

    suspend fun syncSecuritySettings(userId: String, idToken: String) {
        syncMutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    authSessionLogRepository.saveFromIdToken(userId, idToken)
                    // 1. Try to fetch from server
                    val serverSettings = securityPolicyHandler.getSecuritySettings(idToken)
                    if (serverSettings != null) {
                        Log.d(TAG, "✅ Server settings loaded")
                        // 2. Save encrypted in Room
                        roomUseCase.saveSecuritySettings(userId, serverSettings)
                        val roomSnapshot = roomUseCase.getSecuritySettings(userId)
                        // 3. Legacy compatibility mirror (Room -> DataStore)
                        val merged = mirrorToDataStoreIfEnabled(serverSettings)
                        securityPreference.saveLastSyncedTimestamp()
                        logParity(
                            source = "sync_manager_online",
                            server = serverSettings,
                            room = roomSnapshot,
                            localMirror = merged
                        )
                    } else {
                        // Server unreachable → fallback to Room
                        Log.w(TAG, "⚠️ Server settings failed, using local Room")

                        val cached = roomUseCase.getSecuritySettings(userId)
                        if (cached != null) {
                            val merged = mirrorToDataStoreIfEnabled(cached)
                            logParity(
                                source = "sync_manager_offline",
                                server = null,
                                room = cached,
                                localMirror = merged
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to sync", e)
                }
            }
        }
    }
}
