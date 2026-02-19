package net.metalbrain.paysmart.core.session

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.domain.room.RoomUseCase
import net.metalbrain.paysmart.domain.usecase.SecurityUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionObserver @Inject constructor(
    private val auth: FirebaseAuth,
    private val securityUseCase: SecurityUseCase,
    private val roomUseCase: RoomUseCase
) {

    private var hasSyncedOnce = false

    fun startObserving(applicationScope: CoroutineScope) {
        applicationScope.launch(Dispatchers.Default) {
            observeUntilUnlockedAndSynced()
        }
    }

    private suspend fun observeUntilUnlockedAndSynced() {
        while (true) {
            try {
                val isUnlocked = !securityUseCase.isLocked()
                val isRoomReady = roomUseCase.isReady()

                if (isUnlocked && isRoomReady) {
                    val user = auth.currentUser
                    val token = user?.getIdToken(false)?.await()?.token
                    if (user != null && token != null) {
                        if (!hasSyncedOnce) {
                            Log.d("SessionObserver", "🔐 Syncing post-unlock settings")
                            val syncResult = securityUseCase.syncSecuritySettings(user.uid, token)
                            if (syncResult.isSuccess) {
                                hasSyncedOnce = true
                            } else {
                                Log.w("SessionObserver", "❌ Synced: will retry on next observation cycle.")
                            }
                        } else {
                            Log.d("SessionObserver", "✅ Already synced, skipping")
                        }
                    }
                    return // stop observing
                }

            } catch (e: Exception) {
                Log.e("SessionObserver", "❌ Error during session sync observation", e)
            }

            delay(300)
        }
    }
}
