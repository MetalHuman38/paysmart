package net.metalbrain.paysmart.ui.profile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.ui.profile.identity.config.ConfigurableIdentityImageAuthenticityDetector
import net.metalbrain.paysmart.ui.profile.identity.config.HttpRemoteIdentityImageAttestationApi
import net.metalbrain.paysmart.ui.profile.identity.uploadhelpers.DeviceAttestationProvider
import net.metalbrain.paysmart.ui.profile.identity.provider.FallbackDeviceAttestationProvider
import net.metalbrain.paysmart.ui.profile.identity.provider.IdentityDocumentTextExtractor
import net.metalbrain.paysmart.ui.profile.identity.provider.IdentityImageAuthenticityDetector
import net.metalbrain.paysmart.ui.profile.identity.provider.OnDeviceIdentityDocumentTextExtractor
import net.metalbrain.paysmart.ui.profile.identity.uploadhelpers.IdentityUploadRepository
import net.metalbrain.paysmart.ui.profile.identity.provider.RemoteIdentityImageAttestationApi
import net.metalbrain.paysmart.ui.profile.identity.provider.RemoteIdentityUploadRepository
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
