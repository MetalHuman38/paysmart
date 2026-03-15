package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.ui.components.PrimaryButton


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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag(INVOICE_WEEKLY_LIST_TAG),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text(text = stringResource(R.string.invoice_weekly_subtitle)) }

            item {
                InvoiceWeeklyVenueSelector(
                    state = state,
                    onVenueSelected = onVenueSelected
                )
            }

            item {
                InvoiceWeeklyMetaFields(
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
                    enabled = !state.isFinalizing && state.canFinalize,
                    modifier = Modifier.testTag(INVOICE_FINALIZE_BUTTON_TAG)
                )
            }

            item {
                InvoiceWeeklyHistorySection(
                    invoices = state.finalizedInvoices,
                    isLoading = state.isInvoiceHistoryLoading,
                    onOpenInvoice = onOpenInvoice
                )
            }
        }
    }
}
