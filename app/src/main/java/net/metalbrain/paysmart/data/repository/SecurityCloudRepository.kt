package net.metalbrain.paysmart.data.repository

import net.metalbrain.paysmart.domain.model.SecuritySettings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.domain.auth.UserManager

class SecurityCloudRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userManager: UserManager
) : SecurityRepository {

    private fun userDoc() = firestore
        .collection("users")
        .document(userManager.uid)
        .collection("security")
        .document("settings")


    /** Read Only **/

    override suspend fun getSettings(): Result<SecuritySettings?> =
        runCatching {
            userDoc().get().await().toObject<SecuritySettings>()
        }

    override suspend fun updateOnboardingCompleted(
        completed: Map<String, Boolean>
    ): Result<Unit> =
        runCatching {
            userDoc()
                .set(mapOf("onboardingCompleted" to completed), SetOptions.merge())
                .await()
        }
}
