package net.metalbrain.paysmart.data.repository

import net.metalbrain.paysmart.domain.model.SecuritySettings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class SecurityCloudRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun userDoc(uid: String) = firestore
        .collection("users")
        .document(uid)
        .collection("security")
        .document("settings")

    /** Read Only **/
    suspend fun getSettings(uid: String): SecuritySettings? {
        val snapshot = userDoc(uid).get().await()
        return snapshot.toObject(SecuritySettings::class.java)
    }

    suspend fun updateOnboardingCompleted(
        uid: String,
        completed: Map<String, Boolean>
    ) {
        userDoc(uid)
            .set(mapOf("onboardingCompleted" to completed), SetOptions.merge())
            .await()
    }
}
