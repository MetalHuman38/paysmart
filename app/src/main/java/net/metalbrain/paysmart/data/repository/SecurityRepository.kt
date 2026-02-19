package net.metalbrain.paysmart.data.repository

import android.util.Log
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.auth.AllowFederatedLinkingHandler
import net.metalbrain.paysmart.core.auth.SecuritySettingsHandler
import net.metalbrain.paysmart.core.security.SecurityMigrationFlags
import net.metalbrain.paysmart.core.security.SecurityParity
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.domain.room.RoomUseCase

class SecurityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val roomUseCase: RoomUseCase,
    private val securityPolicyHandler: SecuritySettingsHandler,
    private val allowFederatedLinkingHandler: AllowFederatedLinkingHandler,
    private val securityPreference: SecurityPreference,
    private val securityParity: SecurityParity
) : SecurityRepositoryInterface {
    private companion object {
        const val TAG = "SecurityRepo"
    }

    private fun userDoc(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("security")
        .document("settings")

    private fun mapRoomModelToLocal(
        roomModel: SecuritySettingsModel,
        currentLocal: LocalSecuritySettingsModel
    ): LocalSecuritySettingsModel {
        return currentLocal.copy(
            allowFederatedLinking = roomModel.allowFederatedLinking ?: currentLocal.allowFederatedLinking,
            biometricsRequired = roomModel.biometricsRequired ?: currentLocal.biometricsRequired,
            biometricsEnabled = roomModel.biometricsEnabled ?: currentLocal.biometricsEnabled,
            biometricsEnabledAt = roomModel.biometricsEnabledAt ?: currentLocal.biometricsEnabledAt,
            passcodeEnabled = roomModel.passcodeEnabled ?: currentLocal.passcodeEnabled,
            passwordEnabled = roomModel.passwordEnabled ?: currentLocal.passwordEnabled,
            localPassCodeSetAt = roomModel.localPassCodeSetAt ?: currentLocal.localPassCodeSetAt,
            localPasswordSetAt = roomModel.localPasswordSetAt ?: currentLocal.localPasswordSetAt,
            lockAfterMinutes = roomModel.lockAfterMinutes ?: currentLocal.lockAfterMinutes,
            tosAcceptedAt = roomModel.tosAcceptedAt ?: currentLocal.tosAcceptedAt,
            kycStatus = roomModel.kycStatus ?: currentLocal.kycStatus,
            onboardingRequired = roomModel.onboardingRequired ?: currentLocal.onboardingRequired,
            onboardingCompleted = roomModel.onboardingCompleted ?: currentLocal.onboardingCompleted,
            updatedAt = roomModel.updatedAt ?: currentLocal.updatedAt,
            hasAddedHomeAddress = roomModel.hasAddedHomeAddress ?: currentLocal.hasAddedHomeAddress,
            hasVerifiedEmail = roomModel.hasVerifiedEmail,
            emailVerificationSentAt = roomModel.emailVerificationSentAt ?: currentLocal.emailVerificationSentAt,
            emailToVerify = roomModel.emailToVerify ?: currentLocal.emailToVerify,
            hasVerifiedIdentity = roomModel.hasVerifiedIdentity ?: currentLocal.hasVerifiedIdentity,
            // Session lock is ephemeral-local and should not be replaced by durable store snapshots.
            sessionLocked = currentLocal.sessionLocked,
            killSwitchActive = roomModel.killswitch || currentLocal.killSwitchActive,
            lastSynced = System.currentTimeMillis()
        )
    }

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

        val roomParity = securityParity.assertServerRoomParity(server, room)
        if (roomParity.matches) {
            Log.d(TAG, "parity_assertion_ok[$source:server_room]")
        } else {
            Log.w(
                TAG,
                "parity_assertion_failed[$source:server_room] mismatches=${securityParity.mismatchLabels("room", roomParity.mismatches)}"
            )
        }

        if (localMirror != null) {
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

    override suspend fun isLoggedIn(): Boolean {
         return try {
             firebaseAuthRepository.getCurrentSessionOrThrow()
             true
         } catch (e: Exception) {
             false
         }
    }

    override suspend fun saveSecuritySettings(userId: String, model: SecuritySettingsModel): Result<Unit> {
        Log.d("SecuritySync", "→ Fetching security settings for userId=$userId")
        return try {
            roomUseCase.saveSecuritySettings(userId, model)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncSecuritySettings(userId: String, idToken: String): Result<Unit> {
        return try {
            val server = securityPolicyHandler.getSecuritySettings(idToken)
                ?: return Result.failure(Exception("Server returned null"))

            roomUseCase.saveSecuritySettings(userId, server)
            val room = roomUseCase.getSecuritySettings(userId)

            val merged = if (SecurityMigrationFlags.legacyDatastoreMirrorEnabled) {
                val local = securityPreference.loadLocalSecurityState()
                val nextLocal = securityPreference.mergeServerWithLocal(server, local)
                securityPreference.saveCloudSecuritySettings(server)
                securityPreference.saveLocalSecurityState(nextLocal)
                nextLocal
            } else {
                null
            }

            logParity(
                source = "repo_sync",
                server = server,
                room = room,
                localMirror = merged
            )
            securityPreference.saveLastSyncedTimestamp()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Read Only **/

    override suspend fun getSettings(userId: String): Result<SecuritySettingsModel?> =
        runCatching {
            userDoc(userId).get().await().toObject<SecuritySettingsModel>()
        }

    override suspend fun getLocalSettings(userId: String): Result<LocalSecuritySettingsModel?> =
        runCatching {
            val useRoomAuthoritative = SecurityMigrationFlags.shouldUseRoomAuthoritative(userId)
            if (useRoomAuthoritative) {
                val currentLocal = securityPreference.loadLocalSecurityState()
                val roomModel = roomUseCase.getSecuritySettings(userId)
                if (roomModel != null) {
                    return@runCatching mapRoomModelToLocal(roomModel, currentLocal)
                }
                Log.w(
                    TAG,
                    "Room-authoritative read enabled but no Room row for userId=$userId. Falling back to DataStore."
                )
                return@runCatching currentLocal
            }
            userDoc(userId).get().await().toObject<LocalSecuritySettingsModel?>()
        }

    override suspend fun updateOnboardingCompleted(
        userId: String,
        completed: Map<String, Boolean>
    ): Result<Unit> =
        runCatching {
            userDoc(userId)
                .set(mapOf("onboardingCompleted" to completed), SetOptions.merge())
                .await()
        }

    override suspend fun allowFederatedLinking(idToken: String): Result<Unit> {
        return try {
            val enabled = allowFederatedLinkingHandler.allowLinking(idToken)
            if (!enabled) {
                return Result.failure(
                    IllegalStateException("Unable to enable federated linking policy")
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
