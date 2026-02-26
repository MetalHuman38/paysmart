package net.metalbrain.paysmart.core.features.account.security.repository

import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel

interface SecurityRepositoryInterface {

    suspend fun isLoggedIn(): Boolean

    suspend fun saveSecuritySettings(userId: String, model: SecuritySettingsModel): Result<Unit>

    suspend fun syncSecuritySettings(userId: String, idToken: String): Result<Unit>

    suspend fun getSettings(userId: String): Result<SecuritySettingsModel?>

    suspend fun getLocalSettings(userId: String): Result<LocalSecuritySettingsModel?>

    suspend fun updateOnboardingCompleted(userId: String, completed: Map<String, Boolean>
    ): Result<Unit>

    suspend fun allowFederatedLinking(idToken: String): Result<Unit>

    suspend fun setHomeAddressVerified(userId: String, idToken: String): Result<Unit>
}
