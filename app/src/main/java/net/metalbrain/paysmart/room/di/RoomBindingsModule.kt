// File: net.metalbrain.paysmart.room.di.RoomBindingsModule.kt
package net.metalbrain.paysmart.room.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.domain.room.NativeRoomUseCase
import net.metalbrain.paysmart.domain.room.RoomUseCase

@Module
@InstallIn(SingletonComponent::class)
abstract class RoomBindingsModule {
    @Binds
    abstract fun bindRoomUseCase(
        impl: NativeRoomUseCase
    ): RoomUseCase
}
