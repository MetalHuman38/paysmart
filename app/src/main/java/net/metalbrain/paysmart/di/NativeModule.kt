package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.auth.BcryptPasswordHasher
import net.metalbrain.paysmart.core.auth.NativePasswordHasher
import net.metalbrain.paysmart.domain.crypto.CryptoUseCase
import net.metalbrain.paysmart.domain.crypto.NativeCryptoUseCase


@Module
@InstallIn(SingletonComponent::class)
abstract class CryptoModule {

    @Binds
    abstract fun bindCryptoUseCase(
        impl: NativeCryptoUseCase
    ): CryptoUseCase
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PasswordModule {
    @Binds
    abstract fun bindPasswordHasher(
        impl: NativePasswordHasher
    ): BcryptPasswordHasher
}
