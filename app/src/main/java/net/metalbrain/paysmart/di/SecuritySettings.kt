package net.metalbrain.paysmart.di

import dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.features.account.security.remote.SecuritySettingsHandler
import net.metalbrain.paysmart.core.features.account.security.remote.SecuritySettingsPolicy
import net.metalbrain.paysmart.data.repository.AuthSessionLogRepository
import net.metalbrain.paysmart.core.features.account.security.data.SecurityParity
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
import net.metalbrain.paysmart.core.features.account.security.manager.SecuritySyncManager
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
        appCheckTokenProvider: AppCheckTokenProvider
    ): SecuritySettingsPolicy {
        return SecuritySettingsPolicy(
            config = config,
            appCheckTokenProvider = appCheckTokenProvider
        )
    }

    @Provides
    fun provideSecuritySettingsHandler(
        policy: SecuritySettingsPolicy
    ): SecuritySettingsHandler {
        return SecuritySettingsHandler(policy)
    }
}
