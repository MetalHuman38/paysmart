package net.metalbrain.paysmart.core.session

interface SessionUseCase {
    suspend fun isLocked(): Boolean
    suspend fun unlockSession()
    suspend fun lockSession()
    suspend fun registerUserInteraction()
    suspend fun markPasscodeEnabledOnServer()
    suspend fun getPasscodeEnabledOnServer(): Boolean
    suspend fun hasPasscode(): Boolean
    suspend fun hasPassword(): Boolean
    suspend fun shouldPromptBiometric(): Boolean
    suspend fun isBiometricCompleted(): Boolean
    suspend fun shouldPromptPasscode(): Boolean
}
