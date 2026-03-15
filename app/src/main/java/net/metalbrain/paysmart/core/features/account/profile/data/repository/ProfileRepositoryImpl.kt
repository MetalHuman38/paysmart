package net.metalbrain.paysmart.core.features.account.profile.data.repository

import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import net.metalbrain.paysmart.core.features.account.profile.data.storage.ProfilePhotoStorage
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileAccountState
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileMissingItem
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileNextStep
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.core.features.account.security.repository.SecurityRepositoryInterface
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.ProfileDetailsDraft

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val profileCacheRepository: UserProfileCacheRepository,
    private val profilePhotoStorage: ProfilePhotoStorage,
    private val securityRepository: SecurityRepositoryInterface,
    private val securityPreference: SecurityPreference
) : ProfileRepository {


    override fun observeProfileState(): Flow<ProfileAccountState> {
        return authRepository.authChanges
            .flatMapLatest { isLoggedIn ->
                if (!isLoggedIn) {
                    return@flatMapLatest flow {
                        emit(ProfileAccountState(loading = false, authenticated = false))
                    }
                }

                val authUser = authRepository.currentUser
                val uid = authUser?.uid ?: return@flatMapLatest flow {
                    emit(ProfileAccountState(loading = false, authenticated = false))
                }

                flow {
                    profileCacheRepository.ensureSeed(
                        uid = uid,
                        displayName = authUser.displayName,
                        email = authUser.email,
                        phoneNumber = authUser.phoneNumber,
                        photoURL = authUser.photoUrl?.toString()
                    )

                    val remoteRefresh = userProfileRepository.watchByUid(uid)
                        .onEach { remote ->
                            if (remote != null) {
                                profileCacheRepository.upsertFromRemote(remote)
                            }
                        }
                        .map { }
                        .onStart { emit(Unit) }
                        .catch { emit(Unit) }

                    val profileFlow = profileCacheRepository.observeByUid(uid)
                    val securityFlow = securityPreference.localSecurityStateFlow

                    emitAllState(profileFlow, securityFlow, remoteRefresh).collect { emit(it) }
                }
            }
            .catch {
                emit(ProfileAccountState(loading = false, authenticated = false))
            }
    }

    override suspend fun saveProfileDraft(draft: ProfileDetailsDraft): Result<Unit> {
        val uid = authRepository.currentUser?.uid
            ?: return Result.failure(IllegalStateException("No authenticated user"))

        return runCatching {
            profileCacheRepository.upsertLocalProfileDetails(uid, draft)
        }
    }

    override suspend fun markHomeAddressVerified(): Result<Unit> {
        return runCatching {
            val session = authRepository.getCurrentSessionOrThrow()
            securityRepository.setHomeAddressVerified(
                userId = session.user.uid,
                idToken = session.idToken
            ).getOrThrow()
        }
    }

    override suspend fun savePresetAvatar(token: String): Result<Unit> {
        val uid = authRepository.currentUser?.uid
            ?: return Result.failure(IllegalStateException("No authenticated user"))

        return runCatching {
            val existingPhotoUrl = profileCacheRepository.getPhotoUrl(uid)
            profileCacheRepository.updatePhotoUrl(uid, token)
            if (existingPhotoUrl.isRemoteProfilePhotoUrl()) {
                runCatching {
                    profilePhotoStorage.delete(uid)
                    userProfileRepository.updatePhotoUrl(uid, null)
                }
            }
        }
    }

    override suspend fun uploadProfilePhoto(
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): Result<Unit> {
        val uid = authRepository.currentUser?.uid
            ?: return Result.failure(IllegalStateException("No authenticated user"))

        return runCatching {
            val photoUrl = profilePhotoStorage.upload(
                uid = uid,
                fileName = fileName,
                mimeType = mimeType,
                bytes = bytes
            )
            userProfileRepository.updatePhotoUrl(uid, photoUrl)
            profileCacheRepository.updatePhotoUrl(uid, photoUrl)
        }
    }

    override suspend fun removeProfilePhoto(): Result<Unit> {
        val uid = authRepository.currentUser?.uid
            ?: return Result.failure(IllegalStateException("No authenticated user"))

        return runCatching {
            val existingPhotoUrl = profileCacheRepository.getPhotoUrl(uid)
            profileCacheRepository.updatePhotoUrl(uid, null)
            if (existingPhotoUrl.isRemoteProfilePhotoUrl()) {
                runCatching {
                    profilePhotoStorage.delete(uid)
                    userProfileRepository.updatePhotoUrl(uid, null)
                }
            } else {
                userProfileRepository.updatePhotoUrl(uid, null)
            }
        }
    }

    private fun emitAllState(
        profileFlow: Flow<AuthUserModel?>,
        securityFlow: Flow<LocalSecuritySettingsModel>,
        remoteRefresh: Flow<Unit>
    ): Flow<ProfileAccountState> {
        return combine(profileFlow, securityFlow, remoteRefresh) { profile, security, _ ->
            val missingItems = collectMissingItems(profile, security)
            val locked = isLocked(profile, security, missingItems)

            ProfileAccountState(
                loading = false,
                authenticated = true,
                user = profile,
                security = security,
                missingItems = missingItems,
                isLocked = locked,
                nextStep = if (locked) null else resolveNextStep(missingItems)
            )
        }.catch {
            emit(
                ProfileAccountState(
                    loading = false,
                    authenticated = true,
                    user = null,
                    security = null,
                    missingItems = emptyList(),
                    nextStep = null,
                    isLocked = false
                )
            )
        }
    }

    private fun collectMissingItems(
        profile: AuthUserModel?,
        security: LocalSecuritySettingsModel
    ): List<ProfileMissingItem> {
        if (profile == null) {
            return listOf(
                ProfileMissingItem.FULL_NAME,
                ProfileMissingItem.DATE_OF_BIRTH,
                ProfileMissingItem.ADDRESS_LINE_1,
                ProfileMissingItem.CITY,
                ProfileMissingItem.EMAIL_ADDRESS,
                ProfileMissingItem.PHONE_NUMBER,
                ProfileMissingItem.COUNTRY,
                ProfileMissingItem.POSTAL_CODE,
                ProfileMissingItem.VERIFIED_EMAIL,
                ProfileMissingItem.HOME_ADDRESS_VERIFIED,
                ProfileMissingItem.IDENTITY_VERIFIED
            )
        }

        val missing = mutableListOf<ProfileMissingItem>()
        if (profile.displayName.isNullOrBlank()) missing += ProfileMissingItem.FULL_NAME
        if (profile.dateOfBirth.isNullOrBlank()) missing += ProfileMissingItem.DATE_OF_BIRTH
        if (profile.addressLine1.isNullOrBlank()) missing += ProfileMissingItem.ADDRESS_LINE_1
        if (profile.city.isNullOrBlank()) missing += ProfileMissingItem.CITY
        if (profile.email.isNullOrBlank()) missing += ProfileMissingItem.EMAIL_ADDRESS
        if (profile.phoneNumber.isNullOrBlank()) missing += ProfileMissingItem.PHONE_NUMBER
        if (profile.country.isNullOrBlank()) missing += ProfileMissingItem.COUNTRY
        if (profile.postalCode.isNullOrBlank()) missing += ProfileMissingItem.POSTAL_CODE

        if (!security.hasVerifiedEmail) missing += ProfileMissingItem.VERIFIED_EMAIL
        if (security.hasAddedHomeAddress != true) missing += ProfileMissingItem.HOME_ADDRESS_VERIFIED
        if (!security.hasVerifiedIdentity) missing += ProfileMissingItem.IDENTITY_VERIFIED

        return missing
    }

    private fun isLocked(
        profile: AuthUserModel?,
        security: LocalSecuritySettingsModel,
        missingItems: List<ProfileMissingItem>
    ): Boolean {
        if (profile == null) return false
        if (!security.hasVerifiedIdentity) return false
        if (!security.hasVerifiedEmail) return false
        if (security.hasAddedHomeAddress != true) return false
        return missingItems.isEmpty()
    }

    private fun resolveNextStep(
        missingItems: List<ProfileMissingItem>
    ): ProfileNextStep? {
        return when {
            missingItems.contains(ProfileMissingItem.VERIFIED_EMAIL) ->
                ProfileNextStep.VERIFY_EMAIL

            missingItems.contains(ProfileMissingItem.HOME_ADDRESS_VERIFIED) ->
                ProfileNextStep.COMPLETE_ADDRESS

            missingItems.contains(ProfileMissingItem.IDENTITY_VERIFIED) ->
                ProfileNextStep.VERIFY_IDENTITY

            missingItems.isNotEmpty() ->
                ProfileNextStep.REVIEW_PROFILE

            else -> null
        }
    }
}

private fun String?.isRemoteProfilePhotoUrl(): Boolean {
    return this?.startsWith("http", ignoreCase = true) == true ||
        this?.startsWith("gs://", ignoreCase = true) == true
}
