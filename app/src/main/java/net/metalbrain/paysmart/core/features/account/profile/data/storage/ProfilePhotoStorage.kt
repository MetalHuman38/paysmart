package net.metalbrain.paysmart.core.features.account.profile.data.storage

interface ProfilePhotoStorage {
    suspend fun upload(
        uid: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): String

    suspend fun delete(uid: String)
}
