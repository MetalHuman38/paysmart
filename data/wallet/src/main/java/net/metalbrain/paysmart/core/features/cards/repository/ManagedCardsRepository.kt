package net.metalbrain.paysmart.core.features.cards.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardData
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardStatus
import net.metalbrain.paysmart.core.service.performance.AppPerformanceMonitor
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.room.dao.ManagedCardDao
import net.metalbrain.paysmart.room.entity.ManagedCardEntity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Singleton
class ManagedCardsRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val dao: ManagedCardDao,
    private val performanceMonitor: AppPerformanceMonitor
) : ManagedCardsGateway {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrent(): Flow<List<ManagedCardData>> {
        return authRepository.authChanges.flatMapLatest { isLoggedIn ->
            val userId = authRepository.currentUser?.uid?.trim().orEmpty()
            if (!isLoggedIn || userId.isEmpty()) {
                flowOf(emptyList())
            } else {
                dao.observeByUserId(userId).map { entities ->
                    entities.map { it.toDomain() }
                }
            }
        }
    }

    override suspend fun syncFromServer(): Result<List<ManagedCardData>> = runCatching {
        performanceMonitor.trace(name = "managed_cards_sync") {
            executeCardsRequest(
                url = config.managedCardsUrl,
                method = "GET",
                fallbackMessage = "Unable to load saved cards"
            )
        }
    }

    override suspend fun removeCard(paymentMethodId: String): Result<List<ManagedCardData>> = runCatching {
        performanceMonitor.trace(name = "managed_cards_remove") {
            executeCardsRequest(
                url = config.managedCardDeleteUrl(paymentMethodId),
                method = "DELETE",
                fallbackMessage = "Unable to remove the saved card"
            )
        }
    }

    override suspend fun setDefaultCard(paymentMethodId: String): Result<List<ManagedCardData>> = runCatching {
        performanceMonitor.trace(name = "managed_cards_set_default") {
            executeCardsRequest(
                url = config.managedCardDefaultUrl(paymentMethodId),
                method = "POST",
                fallbackMessage = "Unable to update the default card"
            )
        }
    }

    private suspend fun executeCardsRequest(
        url: String,
        method: String,
        fallbackMessage: String
    ): List<ManagedCardData> {
        val session = authRepository.getCurrentSessionOrThrow()
        return withContext(Dispatchers.IO) {
            val requestBody = if (method == "POST") {
                "".toRequestBody("application/json".toMediaTypeOrNull())
            } else {
                null
            }
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${session.idToken}")
                .method(method, requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw parseManagedCardsApiException(
                        statusCode = response.code,
                        rawBody = rawBody,
                        fallbackMessage = fallbackMessage
                    )
                }

                val cards = parseManagedCards(rawBody)
                dao.replaceForUserId(
                    userId = session.user.uid,
                    entities = cards.map { it.toEntity(session.user.uid) }
                )
                cards
            }
        }
    }
}

private fun ManagedCardEntity.toDomain(): ManagedCardData {
    return ManagedCardData(
        id = id,
        provider = provider,
        brand = brand,
        last4 = last4,
        expMonth = expMonth,
        expYear = expYear,
        funding = funding,
        country = country,
        fingerprint = fingerprint,
        isDefault = isDefault,
        status = ManagedCardStatus.fromRaw(status),
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs
    )
}

private fun ManagedCardData.toEntity(userId: String): ManagedCardEntity {
    return ManagedCardEntity(
        userId = userId,
        id = id,
        provider = provider,
        brand = brand,
        last4 = last4,
        expMonth = expMonth,
        expYear = expYear,
        funding = funding,
        country = country,
        fingerprint = fingerprint,
        isDefault = isDefault,
        status = status.name.lowercase(),
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs
    )
}
