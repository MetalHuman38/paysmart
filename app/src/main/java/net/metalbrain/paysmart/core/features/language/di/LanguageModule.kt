package net.metalbrain.paysmart.core.features.language.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.domain.LanguageRepository
import net.metalbrain.paysmart.core.features.language.data.LanguageRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LanguageModule {

    @Provides
    @Singleton
    fun provideLanguageRepository(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>
    ): LanguageRepository = LanguageRepositoryImpl(context, dataStore)
}
