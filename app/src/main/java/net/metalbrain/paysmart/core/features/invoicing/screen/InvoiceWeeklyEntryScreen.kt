package net.metalbrain.paysmart.core.features.invoicing.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceSummary
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupViewModel
import java.time.LocalDate

/**
 * A stateful entry point for the weekly invoice entry screen.
 *
 * It connects the [InvoiceSetupViewModel] to the [InvoiceWeeklyEntryScreen] UI,
 * handling the collection of the UI state and mapping user interactions to
 * ViewModel actions.
 *
 * @param viewModel The ViewModel that manages the state and logic for invoice creation.
 * @param onBack A callback triggered when the user navigates back from this screen.
 */
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
                "redirect_to_venue venueCount=${state.venues.size}"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceWeeklyEntryScreen(
    state: InvoiceSetupUiState,
    onBack: () -> Unit,
    onVenueSelected: (String) -> Unit,
    onInvoiceDateChanged: (String) -> Unit,
    onWeekEndingDateChanged: (String) -> Unit,
    onHourlyRateChanged: (String) -> Unit,
    onShiftDateChanged: (index: Int, value: String) -> Unit,
    onShiftHoursChanged: (index: Int, value: String) -> Unit,
    onFinalize: () -> Unit,
    onOpenInvoice: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invoice_weekly_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isHydrating) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text(text = stringResource(R.string.invoice_weekly_subtitle)) }

            item {
                VenueChips(
                    state = state,
                    onVenueSelected = onVenueSelected
                )
            }

            item {
                InvoiceMetaFields(
                    state = state,
                    onInvoiceDateChanged = onInvoiceDateChanged,
                    onWeekEndingDateChanged = onWeekEndingDateChanged,
                    onHourlyRateChanged = onHourlyRateChanged
                )
            }

            item {
                InvoiceWeeklyShiftRows(
                    rows = state.weeklyRows,
                    onShiftDateChanged = onShiftDateChanged,
                    onShiftHoursChanged = onShiftHoursChanged
                )
            }

            item {
                InvoiceWeeklySummaryCard(
                    totalHours = state.weeklyDraft.totalHours,
                    hourlyRateInput = state.weeklyDraft.hourlyRateInput
                )
            }

            item {
                Text(
                    text = if (state.isPersisting) {
                        stringResource(R.string.invoice_weekly_saving)
                    } else {
                        stringResource(R.string.invoice_weekly_saved)
                    }
                )
            }

            state.error?.let { errorMessage ->
                item {
                    Text(text = errorMessage)
                }
            }

            state.infoMessage?.let { infoMessage ->
                item {
                    Text(text = infoMessage)
                }
            }

            state.finalizedInvoice?.let { finalized ->
                item {
                    Text(
                        text = stringResource(
                            R.string.invoice_weekly_finalize_success,
                            finalized.invoiceNumber
                        )
                    )
                }
            }

            item {
                PrimaryButton(
                    text = if (state.isFinalizing) {
                        stringResource(R.string.invoice_weekly_finalizing)
                    } else {
                        stringResource(R.string.invoice_weekly_finalize_action)
                    },
                    onClick = onFinalize,
                    enabled = !state.isFinalizing && state.canFinalize
                )
            }

            item {
                FinalizedInvoiceHistorySection(
                    invoices = state.finalizedInvoices,
                    isLoading = state.isInvoiceHistoryLoading,
                    onOpenInvoice = onOpenInvoice
                )
            }
        }
    }
}

@Composable
private fun FinalizedInvoiceHistorySection(
    invoices: List<InvoiceSummary>,
    isLoading: Boolean,
    onOpenInvoice: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.invoice_weekly_recent_finalized_title))
        when {
            isLoading -> {
                Text(text = stringResource(R.string.invoice_weekly_recent_finalized_loading))
            }

            invoices.isEmpty() -> {
                Text(text = stringResource(R.string.invoice_weekly_recent_finalized_empty))
            }

            else -> {
                invoices.forEach { invoice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onOpenInvoice(invoice.invoiceId) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.invoice_weekly_recent_finalized_item_title,
                                    invoice.invoiceNumber
                                )
                            )
                            Text(
                                text = stringResource(
                                    R.string.invoice_weekly_recent_finalized_item_subtitle,
                                    invoice.venueName,
                                    invoice.weekEndingDate
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VenueChips(
    state: InvoiceSetupUiState,
    onVenueSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.invoice_weekly_select_venue))
        if (state.venues.isEmpty()) {
            Text(text = stringResource(R.string.invoice_weekly_no_venues))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.venues.forEach { venue ->
                    FilterChip(
                        selected = state.effectiveSelectedVenueId == venue.venueId,
                        onClick = { onVenueSelected(venue.venueId) },
                        label = { Text(venue.venueName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceMetaFields(
    state: InvoiceSetupUiState,
    onInvoiceDateChanged: (String) -> Unit,
    onWeekEndingDateChanged: (String) -> Unit,
    onHourlyRateChanged: (String) -> Unit
) {
    val invoiceFallbackDate = runCatching { LocalDate.parse(state.weeklyDraft.invoiceDate) }
        .getOrDefault(LocalDate.now())
    val weekEndingFallbackDate = runCatching { LocalDate.parse(state.weeklyDraft.weekEndingDate) }
        .getOrDefault(invoiceFallbackDate)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InvoiceDateField(
            value = state.weeklyDraft.invoiceDate,
            onDateSelected = onInvoiceDateChanged,
            label = stringResource(R.string.invoice_weekly_invoice_date_label),
            modifier = Modifier.fillMaxWidth(),
            fallbackDate = invoiceFallbackDate
        )
        InvoiceDateField(
            value = state.weeklyDraft.weekEndingDate,
            onDateSelected = onWeekEndingDateChanged,
            label = stringResource(R.string.invoice_weekly_week_ending_label),
            modifier = Modifier.fillMaxWidth(),
            fallbackDate = weekEndingFallbackDate
        )
        OutlinedTextField(
            value = state.weeklyDraft.hourlyRateInput,
            onValueChange = onHourlyRateChanged,
            label = { Text(stringResource(R.string.invoice_weekly_hourly_rate_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
