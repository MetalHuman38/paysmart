package net.metalbrain.paysmart.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
import net.metalbrain.paysmart.core.features.account.security.repository.SecurityRepositoryInterface
import net.metalbrain.paysmart.domain.security.SecuritySettingsManager
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSecurityUseCase @Inject constructor(
    private val repository: SecurityRepositoryInterface,
    private val manager: SecuritySettingsManager,
    private val securityPrefs: SecurityPreference,
) : SecurityUseCase {

    private val _settingsFlow = MutableStateFlow<SecuritySettingsModel?>(null)
    private val _localSettingsFlow = MutableStateFlow<LocalSecuritySettingsModel?>(null)

    override val settingsFlow = _settingsFlow.asStateFlow()
    override val localSettingsFlow = _localSettingsFlow.asStateFlow()

    private var lastFetchedAt: Long = 0L
    private val minFetchIntervalMillis = 30_000L // 30 seconds


    // Room Cache persistence mechanism
    override suspend fun saveSecuritySettings(userId: String, model: SecuritySettingsModel): Result<Unit> {
        return repository.saveSecuritySettings(userId, model)
    }

    override suspend fun syncSecuritySettings(userId: String, idToken: String): Result<Unit> {
        return repository.syncSecuritySettings(userId, idToken).onSuccess {
            fetchLocalSettings(userId, force = true)
        }
    }

    override suspend fun fetchLocalSettings(userId: String, force: Boolean): Result<Unit> {
        val now = System.currentTimeMillis()
        if (!force && now - lastFetchedAt < minFetchIntervalMillis && localSettingsFlow.value != null) {
            return Result.success(Unit)
        }

        return repository.getLocalSettings(
            userId
        ).map { settings ->
            _localSettingsFlow.value = settings
            settings?.let { securityPrefs.saveLocalSecurityState(it) }
            lastFetchedAt = now
            Result.success(Unit)
        }
    }


    // Server
    override suspend fun fetchCloudSettings(
        userId: String
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        if (now - lastFetchedAt < minFetchIntervalMillis && _settingsFlow.value != null) {
        return Result.success(Unit) // ✅ Skipped
      }
      return repository.getSettings(
          userId
      ).map { settings ->
        _settingsFlow.value = settings
        securityPrefs.saveCloudSecuritySettings(settings)
          // Get current local
          val currentLocal = securityPrefs.loadLocalSecurityState()
          // Merge
          val merged = securityPrefs.mergeServerWithLocal(settings, currentLocal)
          // Save merged local state
          securityPrefs.saveLocalSecurityState(merged)
          _localSettingsFlow.value = merged
          lastFetchedAt = now

          Result.success(Unit)
      }
    }

    override suspend fun isLocked(): Boolean =
        manager.isLocked()

    override suspend fun unlockSession() =
        manager.unlockSession()

    override suspend fun shouldPromptBiometric(): Boolean =
        manager.shouldPromptBiometric()

    override suspend fun isBiometricCompleted(): Boolean =
        manager.isBiometricCompleted()


    override suspend fun shouldPromptPasscode(): Boolean =
        manager.shouldPromptPasscode()

    override suspend fun verifyPasscode(passcode: String): Boolean =
        manager.verifyPasscode(passcode)

    override suspend fun hasPasscode(): Boolean =
        manager.hasPasscode()

    override suspend fun hasPassword(): Boolean =
        manager.hasPassword()


    override suspend fun clearPasscode() =
        manager.clearPasscode()
}
