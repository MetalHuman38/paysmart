package net.metalbrain.paysmart.domain.auth.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.domain.auth.FirebaseUserManager
import net.metalbrain.paysmart.domain.auth.UserManager
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides the dependency injection binding for user management
 * using Firebase as the backend service.
 *
 * This module is installed in the [SingletonComponent] to ensure a single instance
 * of the user management logic is used throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object FireBaseUserManageModule {

    @Singleton
    @Provides
    fun provideUserManager(
        impl: FirebaseUserManager
    ): UserManager = impl
}
