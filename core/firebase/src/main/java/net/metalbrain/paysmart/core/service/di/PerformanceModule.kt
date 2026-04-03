package net.metalbrain.paysmart.core.service.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.service.performance.AppPerformanceMonitor
import net.metalbrain.paysmart.core.service.performance.FirebasePerformanceMonitor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PerformanceModule {

    @Binds
    @Singleton
    abstract fun bindAppPerformanceMonitor(
        impl: FirebasePerformanceMonitor
    ): AppPerformanceMonitor
}
