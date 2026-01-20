package net.metalbrain.paysmart.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.auth.PassCodePolicyHandler
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.data.repository.SecurityRepository
import net.metalbrain.paysmart.domain.model.SecuritySettings
import net.metalbrain.paysmart.domain.security.SecuritySettingsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSecurityUseCase @Inject constructor(
    private val repository: SecurityRepository,
    private val manager: SecuritySettingsManager,
    private val securityPrefs: SecurityPreference,
    private val passcodePolicyHandler: PassCodePolicyHandler
) : SecurityUseCase {

    private val _settingsFlow = MutableStateFlow<SecuritySettings?>(null)
    override val settingsFlow = _settingsFlow.asStateFlow()

    private var lastFetchedAt: Long = 0L
    private val minFetchIntervalMillis = 30_000L // 30 seconds


    override suspend fun fetchCloudSettings(): Result<Unit> {
        val now = System.currentTimeMillis()
        if (now - lastFetchedAt < minFetchIntervalMillis && _settingsFlow.value != null) {
        return Result.success(Unit) // ✅ Skipped
      }

      return repository.getSettings().map { settings ->
        _settingsFlow.value = settings
        securityPrefs.saveCloudSecuritySettings(settings)
        lastFetchedAt = now // ✅ Throttle updated
          Result.success(Unit)
      }
    }

    override suspend fun markPasscodeEnabledOnServer(): Result<Unit> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val idToken = user.getIdToken(false).await().token
                ?: return Result.failure(IllegalStateException("Token missing"))

            val success = passcodePolicyHandler.setPassCodeEnabled(idToken)

            if (success) {
                fetchCloudSettings() // ✅ Refresh settings
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server failed to acknowledge passcode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPasscodeEnabledOnServer(): Boolean {
        return try {
            val user = FirebaseAuth.getInstance().currentUser ?: return false
            val idToken = user.getIdToken(false).await().token ?: return false
            passcodePolicyHandler.getPasswordEnabled(idToken)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }



    override suspend fun isLocked(): Boolean =
        manager.isLocked()

    override suspend fun unlockSession() =
        manager.unlockSession()

    override suspend fun verifyPasscode(passcode: String): Boolean =
        manager.verifyPasscode(passcode)

    override suspend fun hasPasscode(): Boolean =
        manager.hasPasscode()

    override suspend fun clearPasscode() =
        manager.clearPasscode()
}
