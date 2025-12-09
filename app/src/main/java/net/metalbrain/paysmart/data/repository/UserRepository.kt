package net.metalbrain.paysmart.data.repository

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.domain.model.AuthUserModel

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

    /**
     * Update progress flags for a user.
     *
     * @param uid The user ID to update.
     */
    suspend fun updateProgressFlags(uid: String, progressFlags: Map<String, Boolean>)

    /**
     * Update the `lastSignedIn` timestamp for a user.
     *
     * @param uid The user ID to update.
     */
    suspend fun touchLastSignedIn(uid: String)
}
