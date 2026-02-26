package net.metalbrain.paysmart.core.features.identity.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.features.identity.config.ConfigurableIdentityImageAuthenticityDetector
import net.metalbrain.paysmart.core.features.identity.config.HttpRemoteIdentityImageAttestationApi
import net.metalbrain.paysmart.core.features.identity.provider.FallbackDeviceAttestationProvider
import net.metalbrain.paysmart.core.features.identity.provider.IdentityDocumentTextExtractor
import net.metalbrain.paysmart.core.features.identity.provider.IdentityImageAuthenticityDetector
import net.metalbrain.paysmart.core.features.identity.provider.OnDeviceIdentityDocumentTextExtractor
import net.metalbrain.paysmart.core.features.identity.provider.RemoteIdentityImageAttestationApi
import net.metalbrain.paysmart.core.features.identity.provider.RemoteIdentityUploadRepository
import net.metalbrain.paysmart.core.features.identity.handoff.IdentityProviderHandoffRepository
import net.metalbrain.paysmart.core.features.identity.handoff.RemoteIdentityProviderHandoffRepository
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.DeviceAttestationProvider
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityUploadRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IdentityUploadModule {

    @Binds
    @Singleton
    abstract fun bindIdentityUploadRepository(
        impl: RemoteIdentityUploadRepository
    ): IdentityUploadRepository

    @Binds
    @Singleton
    abstract fun bindIdentityProviderHandoffRepository(
        impl: RemoteIdentityProviderHandoffRepository
    ): IdentityProviderHandoffRepository

    @Binds
    @Singleton
    abstract fun bindDeviceAttestationProvider(
        impl: FallbackDeviceAttestationProvider
    ): DeviceAttestationProvider

    @Binds
    @Singleton
    abstract fun bindIdentityImageAuthenticityDetector(
        impl: ConfigurableIdentityImageAuthenticityDetector
    ): IdentityImageAuthenticityDetector

    @Binds
    @Singleton
    abstract fun bindRemoteIdentityImageAttestationApi(
        impl: HttpRemoteIdentityImageAttestationApi
    ): RemoteIdentityImageAttestationApi

    @Binds
    @Singleton
    abstract fun bindIdentityDocumentTextExtractor(
        impl: OnDeviceIdentityDocumentTextExtractor
    ): IdentityDocumentTextExtractor
}
