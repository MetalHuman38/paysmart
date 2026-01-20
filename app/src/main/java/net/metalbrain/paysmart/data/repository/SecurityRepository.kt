package net.metalbrain.paysmart.data.repository

import net.metalbrain.paysmart.domain.model.SecuritySettings

interface SecurityRepository {
    suspend fun getSettings(): Result<SecuritySettings?>
    suspend fun updateOnboardingCompleted(completed: Map<String, Boolean>): Result<Unit>
}
