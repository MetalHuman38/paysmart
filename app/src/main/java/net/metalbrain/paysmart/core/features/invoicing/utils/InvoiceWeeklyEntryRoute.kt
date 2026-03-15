package net.metalbrain.paysmart.core.features.invoicing.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceWeeklyEntryScreen
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupViewModel

@Composable
fun InvoiceWeeklyEntryRoute(
    viewModel: InvoiceSetupViewModel,
    onBack: () -> Unit,
    onRequireProfileSetup: () -> Unit,
    onRequireVenueSetup: () -> Unit,
    onOpenInvoice: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(
        state.isHydrating,
        state.effectiveSelectedVenueId,
        state.weeklyDraft.invoiceDate,
        state.weeklyDraft.weekEndingDate,
        state.weeklyDraft.hourlyRateInput,
        state.hasValidInvoiceDate,
        state.hasValidWeekEndingDate,
        state.hasValidHourlyRate,
        state.hasWorkedShift,
        state.canFinalize,
        state.error
    ) {
        val workedRows = state.weeklyRows
            .filter { (it.hoursInput.toDoubleOrNull() ?: 0.0) > 0.0 }
            .joinToString(separator = ";") { row ->
                "${row.dayLabel}:${row.workDate.ifBlank { "blank" }}/${row.hoursInput}"
            }

        Log.d(
            "InvoiceWeeklyDiag",
            "hydrating=${state.isHydrating} " +
                    "profileValid=${state.profileDraft.isValid} " +
                    "venueCount=${state.venues.size} " +
                    "venue=${state.effectiveSelectedVenueId.ifBlank { "blank" }} " +
                    "invoiceDate=${state.weeklyDraft.invoiceDate.ifBlank { "blank" }} " +
                    "weekEnding=${state.weeklyDraft.weekEndingDate.ifBlank { "blank" }} " +
                    "rate=${state.weeklyDraft.hourlyRateInput.ifBlank { "blank" }} " +
                    "validInvoiceDate=${state.hasValidInvoiceDate} " +
                    "validWeekEndingDate=${state.hasValidWeekEndingDate} " +
                    "validHourlyRate=${state.hasValidHourlyRate} " +
                    "hasWorkedShift=${state.hasWorkedShift} " +
                    "canFinalize=${state.canFinalize} " +
                    "workedRows=$workedRows " +
                    "error=${state.error.orEmpty()}"
        )
    }

    LaunchedEffect(state.isHydrating, state.profileDraft.isValid, state.venues.size) {
        if (!state.isHydrating && !state.profileDraft.isValid) {
            Log.d(
                "InvoiceWeeklyDiag",
                "redirect_to_profile profileValid=${state.profileDraft.isValid}"
            )
            onRequireProfileSetup()
        } else if (!state.isHydrating && state.venues.isEmpty()) {
            Log.d(
                "InvoiceWeeklyDiag",
                "redirect_to_venue venueCount=${0}"
            )
            onRequireVenueSetup()
        }
    }

    InvoiceWeeklyEntryScreen(
        state = state,
        onBack = onBack,
        onVenueSelected = viewModel::selectVenue,
        onInvoiceDateChanged = viewModel::updateInvoiceDate,
        onWeekEndingDateChanged = viewModel::updateWeekEndingDate,
        onHourlyRateChanged = viewModel::updateHourlyRateInput,
        onShiftDateChanged = viewModel::updateShiftDateAt,
        onShiftHoursChanged = viewModel::updateShiftHoursAt,
        onFinalize = viewModel::finalizeInvoice,
        onOpenInvoice = onOpenInvoice
    )
}
