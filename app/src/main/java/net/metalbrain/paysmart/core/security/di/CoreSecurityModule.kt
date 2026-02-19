package net.metalbrain.paysmart.core.security.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.security.RoomKeyManager
import net.metalbrain.paysmart.core.security.SecurityParity
import net.metalbrain.paysmart.core.security.SecurityPreference
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreSecurityModule {

    @Provides
    @Singleton
    fun provideRoomKeyManager(): RoomKeyManager {
        RoomKeyManager.ensureKeyPairExists()
        return RoomKeyManager
    }

    @Provides
    @Singleton
    fun provideSecurityPrefs(
        @ApplicationContext context: Context
    ): SecurityPreference = SecurityPreference(context)

    @Provides
    @Singleton
    fun provideSecurityParity(): SecurityParity = SecurityParity
}
