package net.metalbrain.paysmart.data.repository

import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

interface SecurityRepositoryInterface {

    suspend fun isLoggedIn(): Boolean
    
    suspend fun saveSecuritySettings(userId: String, model: SecuritySettingsModel): Result<Unit>

    suspend fun syncSecuritySettings(userId: String, idToken: String): Result<Unit>

    suspend fun getSettings(userId: String): Result<SecuritySettingsModel?>

    suspend fun getLocalSettings(userId: String): Result<LocalSecuritySettingsModel?>

    suspend fun updateOnboardingCompleted(userId: String, completed: Map<String, Boolean>
    ): Result<Unit>

    suspend fun allowFederatedLinking(idToken: String): Result<Unit>
}
