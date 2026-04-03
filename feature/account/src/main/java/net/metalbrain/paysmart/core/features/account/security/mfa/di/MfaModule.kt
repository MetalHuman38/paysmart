package net.metalbrain.paysmart.core.features.account.security.mfa.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import net.metalbrain.paysmart.core.features.account.security.mfa.provider.FirebaseMfaEnrollmentProvider
import net.metalbrain.paysmart.core.features.account.security.mfa.provider.FirebaseMfaSignInProvider
import net.metalbrain.paysmart.core.features.account.security.mfa.provider.MfaEnrollmentProvider
import net.metalbrain.paysmart.core.features.account.security.mfa.provider.MfaSignInProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class MfaModule {

    @Binds
    @Singleton
    abstract fun bindMfaEnrollmentProvider(
        impl: FirebaseMfaEnrollmentProvider
    ): MfaEnrollmentProvider

    @Binds
    @Singleton
    abstract fun bindMfaSignInProvider(
        impl: FirebaseMfaSignInProvider
    ): MfaSignInProvider
}
