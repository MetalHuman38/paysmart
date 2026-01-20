package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.domain.auth.SocialAuthUseCase
import net.metalbrain.paysmart.domain.usecase.DefaultSocialAuthUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SocialAuthModule {

    @Binds
    @Singleton
    abstract fun bindSocialAuthUseCase(
        impl: DefaultSocialAuthUseCase
    ): SocialAuthUseCase
}
