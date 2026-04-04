package net.metalbrain.paysmart.core.features.cards.repository

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardData

interface ManagedCardsGateway {
    fun observeCurrent(): Flow<List<ManagedCardData>>

    suspend fun syncFromServer(): Result<List<ManagedCardData>>

    suspend fun removeCard(paymentMethodId: String): Result<List<ManagedCardData>>

    suspend fun setDefaultCard(paymentMethodId: String): Result<List<ManagedCardData>>
}
