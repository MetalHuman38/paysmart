package net.metalbrain.paysmart.data.repository

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.room.doa.UserProfileCacheDao
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
        dao.upsert(
            UserProfileCacheEntity(
                userId = user.uid,
                displayName = resolveDisplayName(user.displayName, user.phoneNumber),
                email = user.email,
                phoneNumber = user.phoneNumber,
                photoURL = user.photoURL
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
}

private fun UserProfileCacheEntity.toDomain(): AuthUserModel {
    return AuthUserModel(
        uid = userId,
        displayName = displayName,
        photoURL = photoURL,
        email = email,
        phoneNumber = phoneNumber
    )
}
