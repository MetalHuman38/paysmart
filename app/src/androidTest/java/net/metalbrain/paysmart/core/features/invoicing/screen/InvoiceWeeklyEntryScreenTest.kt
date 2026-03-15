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
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceVenueDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceWeeklyDraft
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
            var state by remember { mutableStateOf(seedState()) }
            PaysmartTheme {
                InvoiceWeeklyEntryScreen(
                    state = state,
                    onBack = {},
                    onVenueSelected = { venueId ->
                        state = state.copy(
                            weeklyDraft = state.weeklyDraft.copy(selectedVenueId = venueId).withFullWeek()
                        )
                    },
                    onInvoiceDateChanged = {
                        state = state.copy(weeklyDraft = state.weeklyDraft.copy(invoiceDate = it).withFullWeek())
                    },
                    onWeekEndingDateChanged = {
                        state = state.copy(weeklyDraft = state.weeklyDraft.copy(weekEndingDate = it).withFullWeek())
                    },
                    onHourlyRateChanged = {
                        state = state.copy(weeklyDraft = state.weeklyDraft.copy(hourlyRateInput = it).withFullWeek())
                    },
                    onShiftDateChanged = { index, value ->
                        state = state.withRowDate(index, value)
                    },
                    onShiftHoursChanged = { index, value ->
                        state = state.withRowHours(index, value)
                    },
                    onFinalize = {},
                    onOpenInvoice = {}
                )
            }
        }

        (0..6).forEach { index ->
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

        composeRule.onNodeWithTag(INVOICE_TOTAL_HOURS_TAG)
            .performScrollTo()
            .assertTextEquals(totalHoursLabel)
        composeRule.onNodeWithTag(INVOICE_SUBTOTAL_TAG)
            .performScrollTo()
            .assertTextEquals(subtotalLabel)
    }

    @Test
    fun finalizeEnablesForSingleWorkedDayWhenDatesAndRateAreValid() {
        composeRule.setContent {
            val state = seedState().copy(
                weeklyDraft = InvoiceWeeklyDraft(
                    selectedVenueId = "venue_1",
                    invoiceDate = "2026-03-09",
                    weekEndingDate = "2026-03-08",
                    hourlyRateInput = "14.50",
                    shifts = listOf(
                        InvoiceWeeklyDraft.defaultWeekShifts()[0],
                        InvoiceWeeklyDraft.defaultWeekShifts()[1],
                        InvoiceWeeklyDraft.defaultWeekShifts()[2],
                        InvoiceWeeklyDraft.defaultWeekShifts()[3],
                        InvoiceWeeklyDraft.defaultWeekShifts()[4].copy(hoursInput = "10"),
                        InvoiceWeeklyDraft.defaultWeekShifts()[5],
                        InvoiceWeeklyDraft.defaultWeekShifts()[6]
                    )
                ).withFullWeek()
            )

            PaysmartTheme {
                InvoiceWeeklyEntryScreen(
                    state = state,
                    onBack = {},
                    onVenueSelected = {},
                    onInvoiceDateChanged = {},
                    onWeekEndingDateChanged = {},
                    onHourlyRateChanged = {},
                    onShiftDateChanged = { _, _ -> },
                    onShiftHoursChanged = { _, _ -> },
                    onFinalize = {},
                    onOpenInvoice = {}
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
        composeRule.setContent {
            val state = seedState().copy(
                weeklyDraft = InvoiceWeeklyDraft(
                    selectedVenueId = "",
                    invoiceDate = "2026-03-09",
                    weekEndingDate = "2026-03-08",
                    hourlyRateInput = "14.50",
                    shifts = InvoiceWeeklyDraft.defaultWeekShifts().mapIndexed { index, row ->
                        if (index == 4) row.copy(hoursInput = "10") else row
                    }
                ).withFullWeek()
            )

            PaysmartTheme {
                InvoiceWeeklyEntryScreen(
                    state = state,
                    onBack = {},
                    onVenueSelected = {},
                    onInvoiceDateChanged = {},
                    onWeekEndingDateChanged = {},
                    onHourlyRateChanged = {},
                    onShiftDateChanged = { _, _ -> },
                    onShiftHoursChanged = { _, _ -> },
                    onFinalize = {},
                    onOpenInvoice = {}
                )
            }
        }

        composeRule.onNodeWithTag(INVOICE_WEEKLY_LIST_TAG)
            .performScrollToNode(hasTestTag(INVOICE_FINALIZE_BUTTON_TAG))
        composeRule.onNodeWithTag(INVOICE_FINALIZE_BUTTON_TAG)
            .assertIsEnabled()
    }

    private fun seedState(): InvoiceSetupUiState {
        return InvoiceSetupUiState(
            venues = listOf(InvoiceVenueDraft(venueId = "venue_1", venueName = "Alpha Venue")),
            weeklyDraft = InvoiceWeeklyDraft(
                selectedVenueId = "venue_1",
                hourlyRateInput = "10",
                shifts = InvoiceWeeklyDraft.defaultWeekShifts()
            ).withFullWeek(),
            isHydrating = false
        )
    }
}

private fun InvoiceSetupUiState.withRowDate(index: Int, value: String): InvoiceSetupUiState {
    val updatedRows = weeklyRows.mapIndexed { i, row ->
        if (i == index) row.copy(workDate = value) else row
    }
    return copy(weeklyDraft = weeklyDraft.copy(shifts = updatedRows).withFullWeek())
}

private fun InvoiceSetupUiState.withRowHours(index: Int, value: String): InvoiceSetupUiState {
    val updatedRows = weeklyRows.mapIndexed { i, row ->
        if (i == index) row.copy(hoursInput = value) else row
    }
    return copy(weeklyDraft = weeklyDraft.copy(shifts = updatedRows).withFullWeek())
}
