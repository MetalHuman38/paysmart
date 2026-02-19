package net.metalbrain.paysmart.core.session.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import net.metalbrain.paysmart.core.session.BaseSessionUseCase
import net.metalbrain.paysmart.data.repository.AuthRepository


@Module
@InstallIn(SingletonComponent::class)
object SessionManagementModule {

    @Provides
    @Singleton
    fun provideSessionUseCase(
        authRepository: AuthRepository,
    ): BaseSessionUseCase {
        return BaseSessionUseCase(authRepository)
    }
}
