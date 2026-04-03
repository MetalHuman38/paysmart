package net.metalbrain.paysmart.data.repository

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.domain.model.LaunchInterest

/**
 * Repository interface responsible for managing user profile data.
 *
 * Provides methods for retrieving, updating, and observing user-specific information,
 * including authentication details, profile metadata, and application-specific interests.
 */
interface UserProfileRepository {

    /**
     * Watch a user's profile by UID. Emits `null` if user doc doesn't exist.
     */
    fun watchByUid(uid: String): Flow<AuthUserModel?>

    /**
     * Upsert a new user profile (merges if existing).
     *
     * @param user The user model to write to Firestore.
     * @param providerId Firebase auth provider ID (e.g. "google.com", "phone").
     */
    suspend fun upsertNewUser(user: AuthUserModel, providerId: String)

    suspend fun getOnce(uid: String): AuthUserModel?

    suspend fun updatePhotoUrl(uid: String, photoUrl: String?)

    suspend fun updateLaunchInterest(uid: String, launchInterest: LaunchInterest)

    /**
     * Update the `lastSignedIn` timestamp for a user.
     *
     * @param uid The user ID to update.
     */
    suspend fun touchLastSignedIn(uid: String)
}
