package net.metalbrain.paysmart.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore
import dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.data.repository.LanguageRepositoryImpl
import net.metalbrain.paysmart.domain.LanguageRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("paysmart_settings")
        }

    @Provides
    @Singleton
    fun provideLanguageRepository(
        dataStore: DataStore<Preferences>
    ): LanguageRepository = LanguageRepositoryImpl(dataStore)
}
