package net.metalbrain.paysmart.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.gson.Gson
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionStatus
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.TransactionHistoryQuery
import net.metalbrain.paysmart.domain.model.TransactionStatusUpdate
import net.metalbrain.paysmart.room.doa.TransactionDao
import net.metalbrain.paysmart.room.entity.TransactionEntity
import net.metalbrain.paysmart.room.query.TransactionPagingQueryFactory
import java.util.Locale

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class LocalTransactionRepository @Inject constructor(
    private val dao: TransactionDao,
    private val userManager: UserManager
) : TransactionRepository, TransactionHistoryRepository {

    override fun observeTransactions(): Flow<List<Transaction>> {
        return userManager.authState.flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> {
                    dao.observeByUserId(auth.uid).map { entities ->
                        entities.map(TransactionEntity::toDomain)
                    }
                }

                else -> flowOf(emptyList())
            }
        }
    }

    override suspend fun getTransactions(): List<Transaction> {
        val userId = currentUserIdOrNull(userManager) ?: return emptyList()
        return dao.getByUserId(userId).map(TransactionEntity::toDomain)
    }

    override fun pagedTransactions(
        query: TransactionHistoryQuery,
        pageSize: Int
    ): Flow<PagingData<Transaction>> {
        val safePageSize = pageSize.coerceAtLeast(1)
        return userManager.authState.flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> {
                    Pager(
                        config = PagingConfig(
                            pageSize = safePageSize,
                            initialLoadSize = safePageSize,
                            enablePlaceholders = false
                        ),
                        pagingSourceFactory = {
                            dao.pagingSource(
                                TransactionPagingQueryFactory.build(
                                    userId = auth.uid,
                                    query = query
                                )
                            )
                        }
                    ).flow.map { pagingData ->
                        pagingData.map(TransactionEntity::toDomain)
                    }
                }

                else -> flowOf(PagingData.empty())
            }
        }
    }

    override fun observeTransaction(transactionId: String): Flow<Transaction?> {
        val normalizedId = transactionId.trim()
        if (normalizedId.isEmpty()) return flowOf(null)

        return userManager.authState.flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> {
                    dao.observeByUserIdAndId(userId = auth.uid, id = normalizedId)
                        .map { entity -> entity?.toDomain() }
                }

                else -> flowOf(null)
            }
        }
    }

    override fun observeAvailableStatuses(): Flow<List<String>> {
        return userManager.authState.flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> dao.observeAvailableStatuses(auth.uid)
                else -> flowOf(emptyList())
            }
        }
    }

    override fun observeAvailableCurrencies(): Flow<List<String>> {
        return userManager.authState.flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> dao.observeAvailableCurrencies(auth.uid)
                else -> flowOf(emptyList())
            }
        }
    }

    override suspend fun recordAddMoneySession(
        session: AddMoneySessionData,
        recordedAtMs: Long
    ) {
        val userId = currentUserIdOrNull(userManager) ?: return
        val sessionId = session.sessionId.trim()
        if (sessionId.isBlank()) return

        val existing = dao.getByUserIdAndId(userId = userId, id = sessionId)
        val nextStatus = session.status.asTransactionStatus()
        val nextTimeline = buildTimeline(
            existingTimeline = existing?.statusTimelineJson.toTimeline(),
            nextStatus = nextStatus,
            recordedAtMs = recordedAtMs
        )

        dao.upsert(
            TransactionEntity(
                userId = userId,
                id = sessionId,
                title = session.provider.transactionTitle(),
                amount = session.amountMinor.toDouble() / 100.0,
                currency = session.currency.trim().uppercase(Locale.US).ifBlank { "GBP" },
                status = nextStatus,
                iconRes = session.provider.transactionIconRes(),
                createdAtMs = existing?.createdAtMs ?: recordedAtMs,
                updatedAtMs = recordedAtMs,
                provider = session.provider.displayName(),
                paymentRail = session.provider.paymentRailLabel(),
                reference = sessionId,
                externalReference = session.paymentIntentId
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: session.flutterwaveTransactionId
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() },
                statusTimelineJson = gson.toJson(nextTimeline)
            )
        )
    }
}

private val gson = Gson()

private fun buildTimeline(
    existingTimeline: List<TransactionStatusUpdate>,
    nextStatus: String,
    recordedAtMs: Long
): List<TransactionStatusUpdate> {
    if (existingTimeline.lastOrNull()?.status == nextStatus) {
        return existingTimeline
    }
    return existingTimeline + TransactionStatusUpdate(
        status = nextStatus,
        timestampMs = recordedAtMs
    )
}

private fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        title = title,
        amount = amount,
        currency = currency,
        status = status,
        iconRes = iconRes,
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs,
        provider = provider,
        paymentRail = paymentRail,
        reference = reference,
        externalReference = externalReference,
        statusTimeline = statusTimelineJson.toTimeline()
    )
}

private fun String?.toTimeline(): List<TransactionStatusUpdate> {
    if (this.isNullOrBlank()) return emptyList()
    return runCatching {
        gson.fromJson(this, Array<TransactionStatusUpdate>::class.java)?.toList().orEmpty()
    }.getOrDefault(emptyList())
}

private fun currentUserIdOrNull(userManager: UserManager): String? {
    return runCatching { userManager.uid }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }
}

private fun AddMoneyProvider.displayName(): String {
    return when (this) {
        AddMoneyProvider.STRIPE -> "Stripe"
        AddMoneyProvider.FLUTTERWAVE -> "Flutterwave"
    }
}

private fun AddMoneyProvider.transactionTitle(): String {
    return when (this) {
        AddMoneyProvider.STRIPE -> "Top up via Stripe"
        AddMoneyProvider.FLUTTERWAVE -> "Top up via Flutterwave"
    }
}

private fun AddMoneyProvider.transactionIconRes(): Int {
    return when (this) {
        AddMoneyProvider.STRIPE -> R.drawable.ic_topup_mastercard
        AddMoneyProvider.FLUTTERWAVE -> R.drawable.ic_topup_bank
    }
}

private fun AddMoneyProvider.paymentRailLabel(): String? {
    return when (this) {
        AddMoneyProvider.STRIPE -> "Card"
        AddMoneyProvider.FLUTTERWAVE -> null
    }
}

private fun AddMoneySessionStatus.asTransactionStatus(): String {
    return when (this) {
        AddMoneySessionStatus.SUCCEEDED -> "Successful"
        AddMoneySessionStatus.FAILED -> "Failed"
        AddMoneySessionStatus.EXPIRED -> "Expired"
        AddMoneySessionStatus.CREATED,
        AddMoneySessionStatus.PENDING -> "In Progress"
    }
}
