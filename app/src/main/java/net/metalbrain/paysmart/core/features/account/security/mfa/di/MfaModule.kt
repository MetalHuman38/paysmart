package net.metalbrain.paysmart.core.features.account.security.mfa.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import net.metalbrain.paysmart.core.features.account.security.mfa.provider.FirebaseMfaEnrollmentProvider
import net.metalbrain.paysmart.core.features.account.security.mfa.provider.MfaEnrollmentProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class MfaModule {

    @Binds
    @Singleton
    abstract fun bindMfaEnrollmentProvider(
        impl: FirebaseMfaEnrollmentProvider
    ): MfaEnrollmentProvider
}
