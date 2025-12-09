package net.metalbrain.paysmart.data.repository

import net.metalbrain.paysmart.domain.model.SecuritySettings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import java.security.Timestamp

class SecurityCloudRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(uid: String) = firestore.collection("security").document(uid)

    suspend fun getSettings(uid: String): SecuritySettings? {
        val snapshot = userDoc(uid).get().await()
        return snapshot.toObject(SecuritySettings::class.java)
    }

    suspend fun setPasscodeEnabled(uid: String, enabled: Boolean) {
        userDoc(uid).set(mapOf("passcodeEnabled" to enabled), SetOptions.merge()).await()
    }

    // Add more functions like setLockAfterMinutes, etc.
    suspend fun setBiometricsRequired(uid: String, required: Boolean) {
        userDoc(uid).set(mapOf("biometricsRequired" to required), SetOptions.merge()).await()
    }

    suspend fun setLockAfterMinutes(uid: String, minutes: Int) {
        userDoc(uid).set(mapOf("lockAfterMinutes" to minutes), SetOptions.merge()).await()
    }

    suspend fun setTosAcceptedAt(uid: String, timestamp: Timestamp) {
        userDoc(uid).set(mapOf("tosAcceptedAt" to timestamp), SetOptions.merge()).await()

    }
}
