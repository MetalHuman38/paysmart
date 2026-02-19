package net.metalbrain.paysmart.di

import dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.auth.SecuritySettingsHandler
import net.metalbrain.paysmart.core.auth.SecuritySettingsPolicy
import net.metalbrain.paysmart.data.repository.AuthSessionLogRepository
import net.metalbrain.paysmart.core.security.SecurityParity
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.core.security.SecuritySyncManager
import net.metalbrain.paysmart.domain.room.RoomUseCase

@Module
@InstallIn(SingletonComponent::class)
object SecuritySettings {

    @Provides
    @Singleton
    fun provideSecuritySyncManager(
        preference: SecurityPreference,
        policy: SecuritySettingsPolicy,
        handler: SecuritySettingsHandler,
        room: RoomUseCase,
        securityParity: SecurityParity,
        authSessionLogRepository: AuthSessionLogRepository
    ): SecuritySyncManager {
        return SecuritySyncManager(
            preference,
            handler,
            room,
            securityParity,
            authSessionLogRepository
        )
    }

    @Provides
    fun provideSecuritySettingsPolicy(
        config: AuthApiConfig,
    ): SecuritySettingsPolicy {
        return SecuritySettingsPolicy(config)
    }

    @Provides
    fun provideSecuritySettingsHandler(
        config: AuthApiConfig,
    ): SecuritySettingsHandler {
        return SecuritySettingsHandler()
    }
}
