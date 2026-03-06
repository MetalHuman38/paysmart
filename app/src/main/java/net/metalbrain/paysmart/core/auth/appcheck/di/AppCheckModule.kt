package net.metalbrain.paysmart.core.auth.appcheck.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.auth.appcheck.provider.FirebaseAppCheckTokenProvider
import javax.inject.Singleton

/**
 * Dagger module that provides dependencies for App Check functionality.
 * This module is installed in the [SingletonComponent] to ensure that the
 * [AppCheckTokenProvider] is available throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppCheckModule {

    @Binds
    @Singleton
    abstract fun bindAppCheckTokenProvider(
        impl: FirebaseAppCheckTokenProvider
    ): AppCheckTokenProvider
}
