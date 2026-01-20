package net.metalbrain.paysmart.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.utils.normalizeProvider

class FirestoreUserProfileRepository @Inject constructor(
    firestore: FirebaseFirestore
) : UserProfileRepository {

    private val users = firestore.collection("users")

    override fun watchByUid(uid: String): Flow<AuthUserModel?> = callbackFlow {
        val listener = users.document(uid).addSnapshotListener { snapshot, _ ->
            val data = snapshot?.data
            data?.let { AuthUserModel.fromMap(it).copy(uid = uid) }
            trySend(snapshot?.toAuthUserModel())
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getOnce(uid: String): AuthUserModel? {
        val snap = users.document(uid).get().await()
        return snap.toAuthUserModel()
    }


    override suspend fun upsertNewUser(user: AuthUserModel, providerId: String) {
        val docRef = users.document(user.uid)

        val normalizedProvider = normalizeProvider(providerId)

        val data = buildMap {
            put("authProvider", normalizedProvider)
            put("email", user.email)
            put("isAnonymous", user.isAnonymous)
            put("providerIds", user.providerIds)
            put("createdAt", FieldValue.serverTimestamp())
            put("lastSignedIn", FieldValue.serverTimestamp())
            put("displayName", user.displayName ?: FieldValue.delete())
            put("photoURL", user.photoURL?.takeIf { it.startsWith("http") } ?: FieldValue.delete())
            put("phoneNumber", user.phoneNumber ?: FieldValue.delete())
            put("tenantId", user.tenantId ?: FieldValue.delete())
        }

        Log.d("UserRepo", "Creating user record: $data")

        docRef.set(data, SetOptions.merge()).await()
    }

    override suspend fun touchLastSignedIn(uid: String) {
        users.document(uid).update("lastSignedIn", FieldValue.serverTimestamp()).await()
    }
}

fun DocumentSnapshot.toAuthUserModel(): AuthUserModel? {
    val data = data ?: return null
    return AuthUserModel.fromMap(data).copy(uid = id)
}
