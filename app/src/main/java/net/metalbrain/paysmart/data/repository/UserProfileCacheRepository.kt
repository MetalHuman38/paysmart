package net.metalbrain.paysmart.data.repository

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.domain.model.ProfileDetailsDraft
import net.metalbrain.paysmart.room.dao.UserProfileCacheDao
import net.metalbrain.paysmart.room.entity.UserProfileCacheEntity

class UserProfileCacheRepository @Inject constructor(
    private val dao: UserProfileCacheDao
) {
    fun observeByUid(uid: String): Flow<AuthUserModel?> {
        return dao.observeByUserId(uid).map { entity -> entity?.toDomain() }
    }

    suspend fun ensureSeed(
        uid: String,
        displayName: String?,
        email: String?,
        phoneNumber: String?,
        photoURL: String?
    ) {
        val existing = dao.getByUserId(uid)
        if (existing != null) {
            return
        }
        dao.upsert(
            UserProfileCacheEntity(
                userId = uid,
                displayName = resolveDisplayName(displayName, phoneNumber),
                email = email,
                phoneNumber = phoneNumber,
                photoURL = photoURL
            )
        )
    }

    suspend fun upsertFromRemote(user: AuthUserModel) {
        val existing = dao.getByUserId(user.uid)

        dao.upsert(
            UserProfileCacheEntity(
                userId = user.uid,
                displayName = user.displayName?.takeIf { it.isNotBlank() }
                    ?: existing?.displayName
                    ?: resolveDisplayName(user.displayName, user.phoneNumber),
                email = user.email ?: existing?.email,
                phoneNumber = user.phoneNumber ?: existing?.phoneNumber,
                photoURL = mergePhotoUrl(existing?.photoURL, user.photoURL),
                dateOfBirth = user.dateOfBirth ?: existing?.dateOfBirth,
                addressLine1 = user.addressLine1 ?: existing?.addressLine1,
                addressLine2 = user.addressLine2 ?: existing?.addressLine2,
                city = user.city ?: existing?.city,
                country = user.country ?: existing?.country,
                postalCode = user.postalCode ?: existing?.postalCode,
            )
        )
    }

    suspend fun getPhotoUrl(uid: String): String? {
        return dao.getByUserId(uid)?.photoURL
    }

    suspend fun updatePhotoUrl(uid: String, photoUrl: String?) {
        val existing = dao.getByUserId(uid)
        val displayName = existing?.displayName
            ?: resolveDisplayName(rawDisplayName = null, phoneNumber = existing?.phoneNumber)

        dao.upsert(
            UserProfileCacheEntity(
                userId = uid,
                displayName = displayName,
                photoURL = photoUrl,
                email = existing?.email,
                phoneNumber = existing?.phoneNumber,
                dateOfBirth = existing?.dateOfBirth,
                addressLine1 = existing?.addressLine1,
                addressLine2 = existing?.addressLine2,
                city = existing?.city,
                country = existing?.country,
                postalCode = existing?.postalCode,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun upsertLocalProfileDetails(uid: String, details: ProfileDetailsDraft) {
        val existing = dao.getByUserId(uid)
        val displayName = details.fullName?.takeIf { it.isNotBlank() }
            ?: existing?.displayName
            ?: resolveDisplayName(rawDisplayName = null, phoneNumber = details.phoneNumber)

        dao.upsert(
            UserProfileCacheEntity(
                userId = uid,
                displayName = displayName,
                photoURL = existing?.photoURL,
                email = details.email ?: existing?.email,
                phoneNumber = details.phoneNumber ?: existing?.phoneNumber,
                dateOfBirth = details.dateOfBirth ?: existing?.dateOfBirth,
                addressLine1 = details.addressLine1 ?: existing?.addressLine1,
                addressLine2 = details.addressLine2 ?: existing?.addressLine2,
                city = details.city ?: existing?.city,
                country = details.country ?: existing?.country,
                postalCode = details.postalCode ?: existing?.postalCode,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun resolveDisplayName(
        rawDisplayName: String?,
        phoneNumber: String?
    ): String {
        val cleanedName = rawDisplayName?.trim().orEmpty()
        if (cleanedName.isNotEmpty()) {
            return cleanedName
        }
        val digits = phoneNumber.orEmpty().filter { it.isDigit() }
        return if (digits.length >= 4) {
            "PaySmart ${digits.takeLast(4)}"
        } else {
            "PaySmart User"
        }
    }

    private fun mergePhotoUrl(existing: String?, remote: String?): String? {
        if (existing.isLocalPresetPhotoUrl()) {
            return existing
        }
        return remote ?: existing
    }
}

private fun String?.isLocalPresetPhotoUrl(): Boolean {
    return this?.startsWith("preset:") == true
}

private fun UserProfileCacheEntity.toDomain(): AuthUserModel {
    return AuthUserModel(
        uid = userId,
        displayName = displayName,
        photoURL = photoURL,
        email = email,
        phoneNumber = phoneNumber,
        dateOfBirth = dateOfBirth,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        city = city,
        country = country,
        postalCode = postalCode
    )
}
