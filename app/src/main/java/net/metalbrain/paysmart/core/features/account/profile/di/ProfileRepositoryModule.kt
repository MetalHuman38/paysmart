package net.metalbrain.paysmart.core.features.account.profile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.features.account.profile.data.repository.ProfileRepository
import net.metalbrain.paysmart.core.features.account.profile.data.repository.ProfileRepositoryImpl
import net.metalbrain.paysmart.core.features.account.profile.data.storage.FirebaseProfilePhotoStorage
import net.metalbrain.paysmart.core.features.account.profile.data.storage.ProfilePhotoStorage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindProfilePhotoStorage(
        impl: FirebaseProfilePhotoStorage
    ): ProfilePhotoStorage
}
