package net.metalbrain.paysmart.core.features.invoicing.viewmodel

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.metalbrain.paysmart.core.auth.AddressLookupResult
import net.metalbrain.paysmart.core.auth.AddressResolverPolicyHandler
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceFinalizeRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceProfileDraftRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceReadRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceSetupPreferenceRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceSetupSelection
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceVenueRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceWeeklyDraftRepository
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceFinalizeResult
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceProfileDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceShiftDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceVenueDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceWeeklyDraft
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.AuthSession
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [InvoiceSetupViewModel].
 *
 * These tests verify the business logic for managing invoice drafts, including:
 * - Draft hydration: Ensuring the UI state correctly reflects saved profile, venue, and weekly draft data.
 * - Invoice finalization: Verifying that once an invoice is finalized, the draft is reset for the
 *   subsequent week while selectively retaining context such as the selected venue and hourly rate.
 *
 * Uses [InvoiceSetupFixture] to manage mock dependencies and coroutine testing utilities.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InvoiceSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `observeDrafts hydrates saved weekly draft and retains selected venue`() = runTest {
        val fixture = InvoiceSetupFixture()
        val savedDraft = validWeeklyDraft(
            selectedVenueId = fixture.venue.venueId,
            invoiceDate = "2026-03-09",
            weekEndingDate = "2026-03-08",
            hourlyRateInput = "18.50",
            workedHours = "7"
        )

        fixture.profileFlow.value = validProfileDraft()
        fixture.venueFlow.value = listOf(fixture.venue)
        fixture.weeklyFlow.value = savedDraft

        val viewModel = fixture.createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isHydrating)
        assertEquals(fixture.venue.venueId, state.effectiveSelectedVenueId)
        assertEquals("2026-03-09", state.weeklyDraft.invoiceDate)
        assertEquals("2026-03-08", state.weeklyDraft.weekEndingDate)
        assertEquals(18.5, state.weeklyDraft.hourlyRateInput.toDouble(), 0.0)
        assertEquals("7", state.weeklyDraft.shifts.first().hoursInput)
    }

    @Test
    fun `finalizeInvoice resets weekly draft for next invoice while retaining venue and rate`() = runTest {
        val fixture = InvoiceSetupFixture()
        val finalized = InvoiceFinalizeResult(
            invoiceId = "inv_123",
            invoiceNumber = "INV-123",
            status = "finalized",
            sequenceNumber = 123,
            totalHours = 8.0,
            hourlyRate = 20.0,
            subtotalMinor = 16_000,
            currency = "GBP",
            venueName = fixture.venue.venueName,
            weekEndingDate = "2026-03-08",
            createdAtMs = 1_700_000_000_000
        )

        fixture.profileFlow.value = validProfileDraft()
        fixture.venueFlow.value = listOf(fixture.venue)
        fixture.weeklyFlow.value = validWeeklyDraft(
            selectedVenueId = fixture.venue.venueId,
            invoiceDate = "2026-03-09",
            weekEndingDate = "2026-03-08",
            hourlyRateInput = "20.00",
            workedHours = "8"
        )
        coEvery {
            fixture.finalizeRepository.finalize(any(), any(), any())
        } returns Result.success(finalized)

        val viewModel = fixture.createViewModel()
        advanceUntilIdle()

        viewModel.finalizeInvoice()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(finalized, state.finalizedInvoice)
        assertEquals(fixture.venue.venueId, state.weeklyDraft.selectedVenueId)
        assertEquals(20.0, state.weeklyDraft.hourlyRateInput.toDouble(), 0.0)
        assertTrue(state.weeklyDraft.invoiceDate.isBlank())
        assertTrue(state.weeklyDraft.weekEndingDate.isBlank())
        assertTrue(state.weeklyDraft.shifts.all { it.workDate.isBlank() && it.hoursInput.isBlank() })

        val persistedDraft = requireNotNull(fixture.weeklyFlow.value)
        assertEquals(fixture.venue.venueId, persistedDraft.selectedVenueId)
        assertEquals(20.0, persistedDraft.hourlyRateInput.toDouble(), 0.0)
        assertTrue(persistedDraft.invoiceDate.isBlank())
        assertTrue(persistedDraft.weekEndingDate.isBlank())
        assertTrue(persistedDraft.shifts.all { it.workDate.isBlank() && it.hoursInput.isBlank() })
    }

    @Test
    fun `observeDrafts merges saved profile into active invoice draft`() = runTest {
        val fixture = InvoiceSetupFixture()

        fixture.venueFlow.value = listOf(fixture.venue)
        fixture.weeklyFlow.value = validWeeklyDraft(
            selectedVenueId = fixture.venue.venueId,
            invoiceDate = "2026-03-09",
            weekEndingDate = "2026-03-08",
            hourlyRateInput = "20.00",
            workedHours = "8"
        )

        val viewModel = fixture.createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.profileDraft.isValid)
        assertFalse(viewModel.uiState.value.canFinalize)

        fixture.profileFlow.value = validProfileDraft()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.profileDraft.isValid)
        assertTrue(state.canFinalize)
        assertEquals("Alex Worker", state.profileDraft.fullName)
        assertEquals("BADGE-1", state.profileDraft.badgeNumber)
    }

    @Test
    fun `searchVenueAddress sends postcode input as postal code`() = runTest {
        val fixture = InvoiceSetupFixture()
        val resolved = addressLookupResult(postCode = "SW1A 1AA")
        coEvery {
            fixture.addressResolverPolicyHandler.resolveAddress(any(), any())
        } returns Result.success(resolved)

        val viewModel = fixture.createViewModel()
        advanceUntilIdle()

        viewModel.updateVenuePostcodeInput("sw1a 1aa")
        viewModel.searchVenueAddress()
        advanceUntilIdle()

        coVerify {
            fixture.addressResolverPolicyHandler.resolveAddress(
                "token",
                match { payload ->
                    payload.line1.isBlank() && payload.postalCode == "SW1A 1AA"
                }
            )
        }
        assertEquals(resolved, viewModel.uiState.value.suggestedVenueAddress)
        assertEquals("SW1A 1AA", viewModel.uiState.value.venuePostcodeInput)
    }

    @Test
    fun `searchVenueAddress treats address field containing only a UK postcode as postal code`() = runTest {
        val fixture = InvoiceSetupFixture()
        coEvery {
            fixture.addressResolverPolicyHandler.resolveAddress(any(), any())
        } returns Result.success(addressLookupResult(postCode = "M1 1AE"))

        val viewModel = fixture.createViewModel()
        advanceUntilIdle()

        viewModel.updateVenueAddressInput("M1 1AE")
        viewModel.searchVenueAddress()
        advanceUntilIdle()

        coVerify {
            fixture.addressResolverPolicyHandler.resolveAddress(
                "token",
                match { payload ->
                    payload.line1.isBlank() && payload.postalCode == "M1 1AE"
                }
            )
        }
    }

    @Test
    fun `finalizeInvoice blocks when required worker profile details are missing`() = runTest {
        val fixture = InvoiceSetupFixture()

        fixture.profileFlow.value = InvoiceProfileDraft(
            fullName = "Alex Worker",
            address = "1 Example Street"
        )
        fixture.venueFlow.value = listOf(fixture.venue)
        fixture.weeklyFlow.value = validWeeklyDraft(
            selectedVenueId = fixture.venue.venueId,
            invoiceDate = "2026-03-09",
            weekEndingDate = "2026-03-08",
            hourlyRateInput = "20.00",
            workedHours = "8"
        )

        val viewModel = fixture.createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canFinalize)

        viewModel.finalizeInvoice()
        advanceUntilIdle()

        assertEquals(
            "Complete invoice profile details before finalizing",
            viewModel.uiState.value.error
        )
        coVerify(exactly = 0) {
            fixture.finalizeRepository.finalize(any(), any(), any())
        }
    }
}

private class InvoiceSetupFixture {
    val userId = "user_123"
    val venue = InvoiceVenueDraft(
        venueId = "venue_1",
        venueName = "Alpha Venue",
        venueAddress = "1 Example Street",
        defaultHourlyRateInput = "20.00"
    )
    val profileFlow = MutableStateFlow<InvoiceProfileDraft?>(null)
    val venueFlow = MutableStateFlow<List<InvoiceVenueDraft>>(emptyList())
    val weeklyFlow = MutableStateFlow<InvoiceWeeklyDraft?>(null)
    val selectionFlow = MutableStateFlow(InvoiceSetupSelection())

    val authRepository = createAuthRepository(userId)
    val userProfileCacheRepository = mockk<UserProfileCacheRepository> {
        every { observeByUid(userId) } returns MutableStateFlow(null)
    }
    val profileRepository = mockk<InvoiceProfileDraftRepository> {
        every { observeByUserId(userId) } returns profileFlow
        coEvery { upsert(any(), any()) } returns Unit
    }
    val venueRepository = mockk<InvoiceVenueRepository> {
        every { observeByUserId(userId) } returns venueFlow
        coEvery { upsert(any(), any()) } returns Unit
    }
    val weeklyDraftRepository = mockk<InvoiceWeeklyDraftRepository> {
        every { observeByUserId(userId) } returns weeklyFlow
        coEvery { upsert(any(), any()) } answers {
            weeklyFlow.value = secondArg()
        }
    }
    val finalizeRepository = mockk<InvoiceFinalizeRepository>()
    val readRepository = mockk<InvoiceReadRepository> {
        coEvery { listFinalized(any()) } returns Result.success(emptyList())
    }
    val setupPreferenceRepository = mockk<InvoiceSetupPreferenceRepository> {
        every { observeSelection() } returns selectionFlow
    }
    val addressResolverPolicyHandler = mockk<AddressResolverPolicyHandler>(relaxed = true)

    fun createViewModel(): InvoiceSetupViewModel {
        return InvoiceSetupViewModel(
            authRepository = authRepository,
            userProfileCacheRepository = userProfileCacheRepository,
            profileRepository = profileRepository,
            venueRepository = venueRepository,
            weeklyDraftRepository = weeklyDraftRepository,
            finalizeRepository = finalizeRepository,
            readRepository = readRepository,
            setupPreferenceRepository = setupPreferenceRepository,
            addressResolverPolicyHandler = addressResolverPolicyHandler
        )
    }
}

private fun createAuthRepository(userId: String): AuthRepository {
    val firebaseUser = mockk<FirebaseUser>()
    every { firebaseUser.uid } returns userId
    every { firebaseUser.displayName } returns ""
    every { firebaseUser.email } returns ""
    every { firebaseUser.phoneNumber } returns ""

    return mockk(relaxed = true) {
        every { currentUser } returns firebaseUser
        coEvery { getCurrentSessionOrThrow() } returns AuthSession(
            user = firebaseUser,
            idToken = "token"
        )
    }
}

private fun validProfileDraft(): InvoiceProfileDraft {
    return InvoiceProfileDraft(
        fullName = "Alex Worker",
        address = "1 Example Street",
        badgeNumber = "BADGE-1",
        badgeExpiryDate = "2027-03-01",
        utrNumber = "UTR123456",
        email = "alex@example.com"
    )
}

private fun validWeeklyDraft(
    selectedVenueId: String,
    invoiceDate: String,
    weekEndingDate: String,
    hourlyRateInput: String,
    workedHours: String
): InvoiceWeeklyDraft {
    return InvoiceWeeklyDraft(
        selectedVenueId = selectedVenueId,
        invoiceDate = invoiceDate,
        weekEndingDate = weekEndingDate,
        hourlyRateInput = hourlyRateInput,
        shifts = listOf(
            InvoiceShiftDraft(
                dayLabel = "Shift 1",
                workDate = weekEndingDate,
                hoursInput = workedHours
            )
        )
    ).withVisibleShifts()
}

private fun addressLookupResult(postCode: String): AddressLookupResult {
    return AddressLookupResult(
        fullAddress = "Westminster, London, $postCode",
        fullAddressWithHouse = "Westminster, London, $postCode",
        postCode = postCode,
        countryCode = "GB",
        houseInfo = "",
        lat = 51.501,
        lng = -0.141,
        line1 = "Westminster",
        line2 = null,
        city = "London",
        stateOrRegion = "England",
        source = "postcodes_io"
    )
}
