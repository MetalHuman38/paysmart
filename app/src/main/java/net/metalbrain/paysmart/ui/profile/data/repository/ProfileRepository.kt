package net.metalbrain.paysmart.ui.profile.data.repository

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.domain.model.ProfileDetailsDraft
import net.metalbrain.paysmart.ui.profile.state.ProfileAccountState

interface ProfileRepository {
    fun observeProfileState(): Flow<ProfileAccountState>
    suspend fun saveProfileDraft(draft: ProfileDetailsDraft): Result<Unit>
    suspend fun markHomeAddressVerified(): Result<Unit>
}
