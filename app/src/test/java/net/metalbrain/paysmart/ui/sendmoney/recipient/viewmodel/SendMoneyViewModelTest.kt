package net.metalbrain.paysmart.ui.sendmoney.recipient.viewmodel

import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.room.dao.SendMoneyRecipientDraftDao
import net.metalbrain.paysmart.room.entity.SendMoneyRecipientDraftEntity
import net.metalbrain.paysmart.testing.MainDispatcherRule
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.core.features.fx.data.FxQuote
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteDataSource
import net.metalbrain.paysmart.core.features.fx.repository.FxQuoteRepository
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteResult
import net.metalbrain.paysmart.core.features.sendmoney.data.SendMoneyRecipientDraftRepository
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientFlowStep
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientMethod
import net.metalbrain.paysmart.core.features.sendmoney.domain.SendMoneyRecipientDraft
import net.metalbrain.paysmart.core.features.sendmoney.domain.VoltpayLookupRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.viewmodel.SendMoneyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [SendMoneyViewModel].
 *
 * This class verifies the business logic and state transitions within the money transfer flow,
 * including:
 * - Navigation between flow steps (Method Selection, Details, Review, and Done).
 * - Validation logic that prevents proceeding to the next step when details are incomplete.
 * - Persistence and restoration of transfer drafts using [SendMoneyRecipientDraftRepository].
 * - Currency exchange (FX) quote fetching, snapshot persistence, and handling of race
 *   conditions during asynchronous updates.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SendMoneyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `nextStep blocks transition to review until selected method details are valid`() = runTest {
        val userId = "user_123"
        val viewModel = createViewModel(userId)

        advanceUntilIdle()
        assertEquals(RecipientFlowStep.METHOD_PICKER, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        advanceUntilIdle()
        assertEquals(RecipientFlowStep.DETAILS, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        advanceUntilIdle()
        assertEquals(RecipientFlowStep.DETAILS, viewModel.uiState.value.currentStep)
        assertTrue(viewModel.uiState.value.error?.contains("Complete recipient details") == true)

        viewModel.updateVoltpayLookup(VoltpayLookupRecipientForm(voltTag = "receiver_tag"))
        viewModel.nextStep()
        advanceUntilIdle()
        assertEquals(RecipientFlowStep.REVIEW, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        advanceUntilIdle()
        assertEquals(RecipientFlowStep.DONE, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `selectMethod from review returns to details and persists updated draft`() = runTest {
        val userId = "user_123"
        val dao = FakeSendMoneyRecipientDraftDao()
        val repository = SendMoneyRecipientDraftRepository(dao)
        val viewModel = SendMoneyViewModel(
            authRepository = createAuthRepository(userId),
            draftRepository = repository,
            fxQuoteRepository = mockk(relaxed = true)
        )

        advanceUntilIdle()
        viewModel.nextStep()
        viewModel.updateVoltpayLookup(VoltpayLookupRecipientForm(voltTag = "receiver_tag"))
        viewModel.nextStep()
        advanceUntilIdle()
        assertEquals(RecipientFlowStep.REVIEW, viewModel.uiState.value.currentStep)

        viewModel.selectMethod(RecipientMethod.BANK_DETAILS)
        advanceUntilIdle()
        assertEquals(RecipientFlowStep.DETAILS, viewModel.uiState.value.currentStep)
        assertEquals(RecipientMethod.BANK_DETAILS, viewModel.uiState.value.draft.selectedMethod)

        val persisted = repository.getByUserId(userId)
        assertEquals(RecipientFlowStep.DETAILS, persisted?.step)
        assertEquals(RecipientMethod.BANK_DETAILS, persisted?.selectedMethod)
    }

    @Test
    fun `refreshQuote persists quote snapshot with draft`() = runTest {
        val userId = "user_fx"
        val dao = FakeSendMoneyRecipientDraftDao()
        val repository = SendMoneyRecipientDraftRepository(dao)
        val quoteRepository = mockk<FxQuoteRepository>()
        coEvery { quoteRepository.getQuote(any()) } returns Result.success(
            FxQuoteResult(
                quote = FxQuote(
                    sourceCurrency = "GBP",
                    targetCurrency = "EUR",
                    sourceAmount = 100.0,
                    rate = 1.17,
                    recipientAmount = 117.0,
                    fees = emptyList(),
                    guaranteeSeconds = 30,
                    arrivalSeconds = 120,
                    rateSource = "test",
                    updatedAtMs = 1_700_000_000L
                ),
                dataSource = FxQuoteDataSource.SERVER
            )
        )

        val viewModel = SendMoneyViewModel(
            authRepository = createAuthRepository(userId),
            draftRepository = repository,
            fxQuoteRepository = quoteRepository
        )

        advanceUntilIdle()
        repository.upsert(
            userId = userId,
            draft = SendMoneyRecipientDraft(
                sourceAmountInput = "100",
                sourceCurrency = "GBP",
                targetCurrency = "EUR"
            )
        )
        advanceUntilIdle()

        viewModel.refreshQuote()
        advanceUntilIdle()

        assertEquals(FxPaymentMethod.WIRE, viewModel.uiState.value.draft.quoteMethod)
        assertNotNull(viewModel.uiState.value.draft.quoteSnapshot)
        assertEquals(FxQuoteDataSource.SERVER, viewModel.uiState.value.draft.quoteDataSource)

        val rawPersisted = dao.rawByUserId(userId)
        assertNotNull(rawPersisted?.quotePayloadJson)

        val persisted = repository.getByUserId(userId)
        assertNotNull(persisted?.quoteSnapshot)
        assertEquals(FxQuoteDataSource.SERVER, persisted?.quoteDataSource)
    }

    @Test
    fun `refreshQuote keeps latest result when earlier request finishes later`() = runTest {
        val userId = "user_fx_race"
        val dao = FakeSendMoneyRecipientDraftDao()
        val repository = SendMoneyRecipientDraftRepository(dao)
        val quoteRepository = mockk<FxQuoteRepository>()

        coEvery {
            quoteRepository.getQuote(match { it.sourceAmount == 10.0 })
        } coAnswers {
            delay(300)
            Result.success(
                FxQuoteResult(
                    quote = FxQuote(
                        sourceCurrency = "GBP",
                        targetCurrency = "EUR",
                        sourceAmount = 10.0,
                        rate = 1.10,
                        recipientAmount = 11.0,
                        fees = emptyList(),
                        guaranteeSeconds = 30,
                        arrivalSeconds = 120,
                        rateSource = "test",
                        updatedAtMs = 1_700_000_001L
                    ),
                    dataSource = FxQuoteDataSource.SERVER
                )
            )
        }
        coEvery {
            quoteRepository.getQuote(match { it.sourceAmount == 20.0 })
        } coAnswers {
            delay(50)
            Result.success(
                FxQuoteResult(
                    quote = FxQuote(
                        sourceCurrency = "GBP",
                        targetCurrency = "EUR",
                        sourceAmount = 20.0,
                        rate = 1.15,
                        recipientAmount = 23.0,
                        fees = emptyList(),
                        guaranteeSeconds = 30,
                        arrivalSeconds = 120,
                        rateSource = "test",
                        updatedAtMs = 1_700_000_002L
                    ),
                    dataSource = FxQuoteDataSource.SERVER
                )
            )
        }

        val viewModel = SendMoneyViewModel(
            authRepository = createAuthRepository(userId),
            draftRepository = repository,
            fxQuoteRepository = quoteRepository
        )

        repository.upsert(
            userId = userId,
            draft = SendMoneyRecipientDraft(
                sourceAmountInput = "10",
                sourceCurrency = "GBP",
                targetCurrency = "EUR"
            )
        )
        advanceUntilIdle()

        viewModel.refreshQuote() // slow request for amount=10

        repository.upsert(
            userId = userId,
            draft = SendMoneyRecipientDraft(
                sourceAmountInput = "20",
                sourceCurrency = "GBP",
                targetCurrency = "EUR"
            )
        )
        runCurrent()
        viewModel.refreshQuote() // fast request for amount=20

        advanceTimeBy(60)
        runCurrent()

        assertEquals(20.0, viewModel.uiState.value.draft.quoteSnapshot?.sourceAmount)
        assertEquals(23.0, viewModel.uiState.value.draft.quoteSnapshot?.recipientAmount)

        advanceTimeBy(300)
        runCurrent()

        assertEquals(20.0, viewModel.uiState.value.draft.quoteSnapshot?.sourceAmount)
        assertEquals(23.0, viewModel.uiState.value.draft.quoteSnapshot?.recipientAmount)
    }

    private fun createViewModel(userId: String): SendMoneyViewModel {
        val dao = FakeSendMoneyRecipientDraftDao()
        return SendMoneyViewModel(
            authRepository = createAuthRepository(userId),
            draftRepository = SendMoneyRecipientDraftRepository(dao),
            fxQuoteRepository = mockk(relaxed = true)
        )
    }

    private fun createAuthRepository(userId: String): AuthRepository {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns userId

        return mockk(relaxed = true) {
            every { currentUser } returns firebaseUser
        }
    }
}

private class FakeSendMoneyRecipientDraftDao : SendMoneyRecipientDraftDao {
    private val rows = MutableStateFlow<Map<String, SendMoneyRecipientDraftEntity>>(emptyMap())

    override suspend fun upsert(entity: SendMoneyRecipientDraftEntity) {
        rows.value += (entity.userId to entity)
    }

    override suspend fun getByUserId(userId: String): SendMoneyRecipientDraftEntity? {
        return rows.value[userId]
    }

    override fun observeByUserId(userId: String): Flow<SendMoneyRecipientDraftEntity?> {
        return rows.map { data -> data[userId] }
    }

    override suspend fun deleteByUserId(userId: String) {
        rows.value -= userId
    }

    fun rawByUserId(userId: String): SendMoneyRecipientDraftEntity? {
        return rows.value[userId]
    }
}
