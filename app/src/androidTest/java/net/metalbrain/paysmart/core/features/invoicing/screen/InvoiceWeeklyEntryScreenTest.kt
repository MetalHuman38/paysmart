package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceProfileDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceShiftDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceVenueDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceWeeklyDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.toDynamicInvoice
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [InvoiceWeeklyEntryScreen], verifying the behavior of weekly invoice data entry,
 * total hour calculations, and the validation logic for finalizing an invoice.
 *
 * These tests ensure that:
 * - Entering hours updates the total duration and subtotal calculations in real-time.
 * - The "Finalize" action is correctly enabled/disabled based on the validity of the input data
 *   (dates, hourly rate, and at least one shift worked).
 * - Edge cases, such as hydration delays in venue selection, do not incorrectly block finalization.
 */
@RunWith(AndroidJUnit4::class)
class InvoiceWeeklyEntryScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun weeklyEntryUpdatesTotalHoursAndSubtotal() {
        val totalHoursLabel = composeRule.activity.getString(
            R.string.invoice_weekly_total_hours_value,
            "12.00"
        )
        val subtotalLabel = composeRule.activity.getString(
            R.string.invoice_weekly_subtotal_value,
            "120.00"
        )

        composeRule.setContent {
            var state by remember {
                mutableStateOf(
                    seedState(
                        shifts = listOf(
                            InvoiceShiftDraft(dayLabel = "Shift 1"),
                            InvoiceShiftDraft(dayLabel = "Shift 2")
                        )
                    )
                )
            }
            PaysmartTheme {
                InvoiceWeeklyEntryScreen(
                    state = state,
                    onBack = {},
                    onVenueSelected = { venueId ->
                        state = state.withWeeklyDraft(state.weeklyDraft.copy(selectedVenueId = venueId).withVisibleShifts())
                    },
                    onInvoiceDateChanged = { value: String ->
                        state = state.withWeeklyDraft(state.weeklyDraft.copy(invoiceDate = value).withVisibleShifts())
                    },
                    onWeekEndingDateChanged = { value: String ->
                        state = state.withWeeklyDraft(state.weeklyDraft.copy(weekEndingDate = value).withVisibleShifts())
                    },
                    onHourlyRateChanged = { value: String ->
                        state = state.withWeeklyDraft(state.weeklyDraft.copy(hourlyRateInput = value).withVisibleShifts())
                    },
                    onShiftDateChanged = { index: Int, value: String ->
                        state = state.withRowDate(index, value)
                    },
                    onShiftHoursChanged = { index: Int, value: String ->
                        state = state.withRowHours(index, value)
                    },
                    onAddShift = {},
                    onRemoveShift = { _: Int -> },
                    onFinalize = {},
                    onOpenInvoice = { _: String -> }
                )
            }
        }

        (0..1).forEach { index ->
            composeRule.onNodeWithTag(invoiceHoursFieldTag(index))
                .performScrollTo()
                .assertIsDisplayed()
        }

        composeRule.onNodeWithTag(invoiceHoursFieldTag(0))
            .performScrollTo()
            .performTextInput("8")
        composeRule.onNodeWithTag(invoiceHoursFieldTag(1))
            .performScrollTo()
            .performTextInput("4")

        composeRule.onNodeWithTag(INVOICE_WEEKLY_LIST_TAG)
            .performScrollToNode(hasTestTag(INVOICE_TOTAL_HOURS_TAG))
        composeRule.onNodeWithTag(INVOICE_TOTAL_HOURS_TAG)
            .assertTextEquals(totalHoursLabel)
        composeRule.onNodeWithTag(INVOICE_WEEKLY_LIST_TAG)
            .performScrollToNode(hasTestTag(INVOICE_SUBTOTAL_TAG))
        composeRule.onNodeWithTag(INVOICE_SUBTOTAL_TAG)
            .assertTextEquals(subtotalLabel)
    }

    @Test
    fun finalizeEnablesForSingleWorkedDayWhenDatesAndRateAreValid() {
        val draft = InvoiceWeeklyDraft(
            selectedVenueId = "venue_1",
            invoiceDate = "2026-03-09",
            weekEndingDate = "2026-03-08",
            hourlyRateInput = "14.50",
            shifts = listOf(
                InvoiceShiftDraft(
                    dayLabel = "Shift 1",
                    workDate = "2026-03-06",
                    hoursInput = "10"
                )
            )
        ).withVisibleShifts()

        composeRule.setContent {
            val state = seedState().withWeeklyDraft(draft)

            PaysmartTheme {
                InvoiceWeeklyEntryScreen(
                    state = state,
                    onBack = {},
                    onVenueSelected = { _: String -> },
                    onInvoiceDateChanged = { _: String -> },
                    onWeekEndingDateChanged = { _: String -> },
                    onHourlyRateChanged = { _: String -> },
                    onShiftDateChanged = { _: Int, _: String -> },
                    onShiftHoursChanged = { _: Int, _: String -> },
                    onAddShift = {},
                    onRemoveShift = { _: Int -> },
                    onFinalize = {},
                    onOpenInvoice = { _: String -> }
                )
            }
        }

        composeRule.onNodeWithTag(INVOICE_WEEKLY_LIST_TAG)
            .performScrollToNode(hasTestTag(INVOICE_FINALIZE_BUTTON_TAG))
        composeRule.onNodeWithTag(INVOICE_FINALIZE_BUTTON_TAG)
            .assertIsEnabled()
    }

    @Test
    fun finalizeEnablesWhenVenueSelectionHydrationLagsButVenueExists() {
        val draft = InvoiceWeeklyDraft(
            selectedVenueId = "",
            invoiceDate = "2026-03-09",
            weekEndingDate = "2026-03-08",
            hourlyRateInput = "14.50",
            shifts = listOf(
                InvoiceShiftDraft(
                    dayLabel = "Shift 1",
                    workDate = "2026-03-06",
                    hoursInput = "10"
                )
            )
        ).withVisibleShifts()

        composeRule.setContent {
            val state = seedState().withWeeklyDraft(draft)

            PaysmartTheme {
                InvoiceWeeklyEntryScreen(
                    state = state,
                    onBack = {},
                    onVenueSelected = { _: String -> },
                    onInvoiceDateChanged = { _: String -> },
                    onWeekEndingDateChanged = { _: String -> },
                    onHourlyRateChanged = { _: String -> },
                    onShiftDateChanged = { _: Int, _: String -> },
                    onShiftHoursChanged = { _: Int, _: String -> },
                    onAddShift = {},
                    onRemoveShift = { _: Int -> },
                    onFinalize = {},
                    onOpenInvoice = { _: String -> }
                )
            }
        }

        composeRule.onNodeWithTag(INVOICE_WEEKLY_LIST_TAG)
            .performScrollToNode(hasTestTag(INVOICE_FINALIZE_BUTTON_TAG))
        composeRule.onNodeWithTag(INVOICE_FINALIZE_BUTTON_TAG)
            .assertIsEnabled()
    }

    private fun seedState(
        shifts: List<InvoiceShiftDraft> = InvoiceWeeklyDraft.defaultShiftRows()
    ): InvoiceSetupUiState {
        val draft = InvoiceWeeklyDraft(
            selectedVenueId = "venue_1",
            hourlyRateInput = "10",
            shifts = shifts
        ).withVisibleShifts()
        val venues = listOf(InvoiceVenueDraft(venueId = "venue_1", venueName = "Alpha Venue"))
        return InvoiceSetupUiState(
            venues = venues,
            isHydrating = false
        ).withWeeklyDraft(draft)
    }
}

private fun InvoiceSetupUiState.withWeeklyDraft(draft: InvoiceWeeklyDraft): InvoiceSetupUiState {
    val venue = venues.firstOrNull { it.venueId == draft.selectedVenueId }
        ?: venues.firstOrNull()
    val updatedInvoice = draft.toDynamicInvoice(validProfileDraft(), venue)
    return copy(draftInvoice = updatedInvoice, selectedVenueId = draft.selectedVenueId)
}

private fun InvoiceSetupUiState.withRowDate(index: Int, value: String): InvoiceSetupUiState {
    val updatedRows = weeklyRows.mapIndexed { i, row ->
        if (i == index) row.copy(workDate = value) else row
    }
    return withWeeklyDraft(weeklyDraft.copy(shifts = updatedRows).withVisibleShifts())
}

private fun InvoiceSetupUiState.withRowHours(index: Int, value: String): InvoiceSetupUiState {
    val updatedRows = weeklyRows.mapIndexed { i, row ->
        if (i == index) row.copy(hoursInput = value) else row
    }
    return withWeeklyDraft(weeklyDraft.copy(shifts = updatedRows).withVisibleShifts())
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
