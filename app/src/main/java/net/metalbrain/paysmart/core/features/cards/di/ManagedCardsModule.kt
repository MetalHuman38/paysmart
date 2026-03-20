package net.metalbrain.paysmart.core.features.cards.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.features.cards.repository.ManagedCardsGateway
import net.metalbrain.paysmart.core.features.cards.repository.ManagedCardsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagedCardsModule {
    @Binds
    @Singleton
    abstract fun bindManagedCardsGateway(
        repository: ManagedCardsRepository
    ): ManagedCardsGateway
}
