package net.metalbrain.paysmart.core.features.account.profile.data.storage

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseProfilePhotoStorage @Inject constructor(
    private val storage: FirebaseStorage
) : ProfilePhotoStorage {

    override suspend fun upload(
        uid: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): String {
        val reference = storage.reference.child("profilePhotos/$uid/current.jpg")
        val metadata = StorageMetadata.Builder()
            .setContentType(mimeType.ifBlank { "image/jpeg" })
            .build()
        reference.putBytes(bytes, metadata).await()
        return reference.downloadUrl.await().toString()
    }

    override suspend fun delete(uid: String) {
        runCatching {
            storage.reference.child("profilePhotos/$uid/current.jpg").delete().await()
        }.onFailure { error ->
            if (error !is StorageException || error.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                throw error
            }
        }
    }
}
