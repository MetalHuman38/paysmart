package net.metalbrain.paysmart.core.service.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.metalbrain.paysmart.core.service.update.FirebaseRemoteConfigUpdatePolicyConfigProvider
import net.metalbrain.paysmart.core.service.update.FirebaseUpdateAnalyticsLogger
import net.metalbrain.paysmart.core.service.update.PlayCoreUpdateDataSource
import net.metalbrain.paysmart.core.service.update.UpdateAnalyticsLogger
import net.metalbrain.paysmart.core.service.update.UpdateClock
import net.metalbrain.paysmart.core.service.update.UpdateDataSource
import net.metalbrain.paysmart.core.service.update.UpdatePolicyConfigProvider
import net.metalbrain.paysmart.core.service.update.SystemUpdateClock

@Module
@InstallIn(SingletonComponent::class)
abstract class UpdateBindingModule {

    @Binds
    @Singleton
    abstract fun bindUpdateDataSource(
        impl: PlayCoreUpdateDataSource,
    ): UpdateDataSource

    @Binds
    @Singleton
    abstract fun bindUpdatePolicyConfigProvider(
        impl: FirebaseRemoteConfigUpdatePolicyConfigProvider,
    ): UpdatePolicyConfigProvider

    @Binds
    @Singleton
    abstract fun bindUpdateAnalyticsLogger(
        impl: FirebaseUpdateAnalyticsLogger,
    ): UpdateAnalyticsLogger

    @Binds
    @Singleton
    abstract fun bindUpdateClock(
        impl: SystemUpdateClock,
    ): UpdateClock
}

@Module
@InstallIn(SingletonComponent::class)
object UpdateProvisionModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance()
    }
}
