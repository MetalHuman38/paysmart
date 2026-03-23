package net.metalbrain.paysmart.core.features.invoicing.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceDetail
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceDetailUiState
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceDetailViewModel
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun InvoiceDetailRoute(
    invoiceId: String,
    viewModel: InvoiceDetailViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(invoiceId) {
        viewModel.load(invoiceId)
    }
    LaunchedEffect(state.shareUri) {
        val shareUri = state.shareUri ?: return@LaunchedEffect
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
        viewModel.consumeShareUri()
    }
    InvoiceDetailScreen(
        state = state,
        onBack = onBack,
        onPreparePdf = viewModel::preparePdf,
        onDownloadPdf = viewModel::downloadPdf,
        onSharePdf = viewModel::sharePdf
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    state: InvoiceDetailUiState,
    onBack: () -> Unit,
    onPreparePdf: () -> Unit,
    onDownloadPdf: () -> Unit,
    onSharePdf: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invoice_detail_title)) },
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
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val invoice = state.invoice
        if (invoice == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: stringResource(R.string.invoice_detail_error_fallback)
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            item {
                InvoiceGuideCard(
                    title = stringResource(R.string.invoice_detail_intro_title),
                    body = stringResource(R.string.invoice_detail_intro_body)
                )
            }
            state.error?.takeIf { it.isNotBlank() }?.let { errorMessage ->
                item {
                    InvoiceNoticeCard(
                        title = stringResource(R.string.invoice_weekly_status_error),
                        body = errorMessage,
                        tone = InvoiceNoticeTone.Error
                    )
                }
            }
            state.infoMessage?.takeIf { it.isNotBlank() }?.let { infoMessage ->
                item {
                    InvoiceNoticeCard(
                        title = stringResource(R.string.invoice_weekly_status_info),
                        body = infoMessage,
                        tone = InvoiceNoticeTone.Neutral
                    )
                }
            }
            item {
                InvoiceDetailSummaryCard(invoice = invoice)
            }
            item {
                InvoicePdfActionsSection(
                    status = invoice.pdf.status,
                    error = invoice.pdf.error,
                    isPreparing = state.isPreparingPdf,
                    isDownloading = state.isDownloadingPdf,
                    isSharing = state.isSharingPdf,
                    onPreparePdf = onPreparePdf,
                    onDownloadPdf = onDownloadPdf,
                    onSharePdf = onSharePdf
                )
            }
            item {
                InvoiceDetailWorkerCard(invoice = invoice)
            }
        }
    }
}

@Composable
private fun InvoiceDetailSummaryCard(invoice: InvoiceDetail) {
    InvoiceSurfaceCard(tone = InvoiceCardTone.Accent) {
        InvoiceSectionHeading(title = stringResource(R.string.invoice_detail_totals_title))
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
            Text(
                text = stringResource(R.string.invoice_detail_number, invoice.invoiceNumber),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.invoice_detail_status, invoice.status),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.invoice_detail_venue, invoice.venueName),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.invoice_detail_period,
                    invoice.weekly.invoiceDate,
                    invoice.weekEndingDate
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.invoice_detail_hours_rate,
                    invoice.totalHours.toString(),
                    invoice.hourlyRate.toString(),
                    invoice.currency
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.invoice_detail_subtotal,
                    formatMoney(invoice.subtotalMinor, invoice.currency)
                ),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun InvoiceDetailWorkerCard(invoice: InvoiceDetail) {
    InvoiceSurfaceCard(tone = InvoiceCardTone.Muted) {
        InvoiceSectionHeading(title = stringResource(R.string.invoice_detail_worker_title))
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
            Text(
                text = stringResource(R.string.invoice_detail_profile_name, invoice.profile.fullName),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.invoice_detail_profile_email, invoice.profile.email),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatMoney(minor: Int, currency: String): String {
    val major = minor / 100.0
    return String.format(java.util.Locale.US, "%.2f %s", major, currency)
}
