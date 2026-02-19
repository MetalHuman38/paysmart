package net.metalbrain.paysmart.domain.auth.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.domain.auth.FirebaseUserManager
import net.metalbrain.paysmart.domain.auth.UserManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FireBaseUserManageModule {

    @Singleton
    @Provides
    fun provideUserManager(
        impl: FirebaseUserManager
    ): UserManager = impl
}
