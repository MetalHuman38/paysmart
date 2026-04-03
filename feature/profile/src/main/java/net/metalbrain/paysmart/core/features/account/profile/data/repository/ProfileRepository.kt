package net.metalbrain.paysmart.core.features.account.profile.data.repository

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileAccountState
import net.metalbrain.paysmart.domain.model.ProfileDetailsDraft
interface ProfileRepository {
    fun observeProfileState(): Flow<ProfileAccountState>
    suspend fun saveProfileDraft(draft: ProfileDetailsDraft): Result<Unit>
    suspend fun markHomeAddressVerified(): Result<Unit>
    suspend fun savePresetAvatar(token: String): Result<Unit>
    suspend fun uploadProfilePhoto(
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): Result<Unit>
    suspend fun removeProfilePhoto(): Result<Unit>
}
