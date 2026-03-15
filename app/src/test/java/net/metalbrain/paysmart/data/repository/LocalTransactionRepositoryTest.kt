package net.metalbrain.paysmart.data.repository

import androidx.paging.PagingSource
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionStatus
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.room.doa.TransactionDao
import net.metalbrain.paysmart.room.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [LocalTransactionRepository], focusing on the persistence and mapping
 * of transaction data originating from add-money sessions.
 *
 * These tests verify:
 * - Successful mapping of [AddMoneySessionData] to transaction entities.
 * - Deduplication and chronological ordering of the status timeline.
 * - Proper handling of edge cases, such as blank session identifiers.
 * - Integration with the data access object (DAO) using an in-memory implementation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalTransactionRepositoryTest {

    @Test
    fun `recordAddMoneySession maps provider data and dedupes timeline`() = runTest {
        val repository = LocalTransactionRepository(
            dao = InMemoryTransactionDao(),
            userManager = FakeUserManager(uid = "user_123")
        )

        repository.recordAddMoneySession(
            session = session(
                sessionId = "stripe_session_1",
                status = AddMoneySessionStatus.CREATED,
                paymentIntentId = "pi_123"
            ),
            recordedAtMs = 1_700_000_000_000
        )
        repository.recordAddMoneySession(
            session = session(
                sessionId = "stripe_session_1",
                status = AddMoneySessionStatus.PENDING,
                paymentIntentId = "pi_123"
            ),
            recordedAtMs = 1_700_000_000_500
        )
        repository.recordAddMoneySession(
            session = session(
                sessionId = "stripe_session_1",
                status = AddMoneySessionStatus.SUCCEEDED,
                paymentIntentId = "pi_123"
            ),
            recordedAtMs = 1_700_000_001_000
        )

        val transaction = repository.getTransactions().single()

        assertEquals("stripe_session_1", transaction.id)
        assertEquals("Top up via Stripe", transaction.title)
        assertEquals(25.0, transaction.amount, 0.001)
        assertEquals("GBP", transaction.currency)
        assertEquals("Successful", transaction.status)
        assertEquals("Stripe", transaction.provider)
        assertEquals("Card", transaction.paymentRail)
        assertEquals("stripe_session_1", transaction.reference)
        assertEquals("pi_123", transaction.externalReference)
        assertEquals(1_700_000_000_000, transaction.createdAtMs)
        assertEquals(1_700_000_001_000, transaction.updatedAtMs)
        assertEquals(
            listOf("In Progress", "Successful"),
            transaction.statusTimeline.map { it.status }
        )
        assertEquals(
            listOf(1_700_000_000_000, 1_700_000_001_000),
            transaction.statusTimeline.map { it.timestampMs }
        )
    }

    @Test
    fun `recordAddMoneySession ignores blank session ids`() = runTest {
        val repository = LocalTransactionRepository(
            dao = InMemoryTransactionDao(),
            userManager = FakeUserManager(uid = "user_123")
        )

        repository.recordAddMoneySession(
            session = session(sessionId = "   "),
            recordedAtMs = 1_700_000_000_000
        )

        assertTrue(repository.getTransactions().isEmpty())
    }

    @Test
    fun `observeTransaction returns persisted transaction by id`() = runTest {
        val repository = LocalTransactionRepository(
            dao = InMemoryTransactionDao(),
            userManager = FakeUserManager(uid = "user_123")
        )

        repository.recordAddMoneySession(
            session = session(sessionId = "stripe_session_2"),
            recordedAtMs = 1_700_000_100_000
        )

        val transaction = repository.observeTransaction("stripe_session_2").first()

        assertEquals("stripe_session_2", transaction?.id)
        assertEquals("stripe_session_2", transaction?.reference)
    }
}

private fun session(
    sessionId: String,
    status: AddMoneySessionStatus = AddMoneySessionStatus.CREATED,
    paymentIntentId: String? = null,
    provider: AddMoneyProvider = AddMoneyProvider.STRIPE
): AddMoneySessionData {
    return AddMoneySessionData(
        sessionId = sessionId,
        amountMinor = 2_500,
        currency = "GBP",
        status = status,
        expiresAtMs = 1_800_000_000_000,
        provider = provider,
        paymentIntentId = paymentIntentId
    )
}

private class FakeUserManager(
    override val uid: String,
    initialState: AuthState = AuthState.Authenticated(uid)
) : UserManager {
    override val authState: Flow<AuthState> = MutableStateFlow(initialState)

    override fun signOut() = Unit
}

private class InMemoryTransactionDao : TransactionDao {
    private val rows = MutableStateFlow<List<TransactionEntity>>(emptyList())

    override suspend fun upsert(entity: TransactionEntity) {
        rows.value = rows.value
            .filterNot { it.userId == entity.userId && it.id == entity.id } +
            entity
    }

    override fun observeByUserId(userId: String): Flow<List<TransactionEntity>> {
        return rows.map { items ->
            items.filter { it.userId == userId }.sortedTransactions()
        }
    }

    override suspend fun getByUserId(userId: String): List<TransactionEntity> {
        return rows.value.filter { it.userId == userId }.sortedTransactions()
    }

    override fun pagingSource(query: SupportSQLiteQuery): PagingSource<Int, TransactionEntity> {
        throw UnsupportedOperationException("PagingSource is not needed in this unit test")
    }

    override fun observeByUserIdAndId(userId: String, id: String): Flow<TransactionEntity?> {
        return rows.map { items ->
            items.firstOrNull { it.userId == userId && it.id == id }
        }
    }

    override fun observeAvailableStatuses(userId: String): Flow<List<String>> {
        return rows.map { items ->
            items.filter { it.userId == userId }
                .map(TransactionEntity::status)
                .distinct()
                .sorted()
        }
    }

    override fun observeAvailableCurrencies(userId: String): Flow<List<String>> {
        return rows.map { items ->
            items.filter { it.userId == userId }
                .map(TransactionEntity::currency)
                .distinct()
                .sorted()
        }
    }

    override suspend fun getByUserIdAndId(userId: String, id: String): TransactionEntity? {
        return rows.value.firstOrNull { it.userId == userId && it.id == id }
    }
}

private fun List<TransactionEntity>.sortedTransactions(): List<TransactionEntity> {
    return sortedWith(
        compareByDescending<TransactionEntity> { it.createdAtMs }
            .thenByDescending { it.updatedAtMs }
    )
}
