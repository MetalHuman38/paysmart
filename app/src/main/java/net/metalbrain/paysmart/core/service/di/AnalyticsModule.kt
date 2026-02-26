package net.metalbrain.paysmart.core.service.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.service.repositiry.FirebaseAnalyticalServiceInterface
import net.metalbrain.paysmart.core.service.repositiry.FirebaseAnalyticsService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalyticsService(
        impl: FirebaseAnalyticsService
    ): FirebaseAnalyticalServiceInterface {
        return impl
    }
}
