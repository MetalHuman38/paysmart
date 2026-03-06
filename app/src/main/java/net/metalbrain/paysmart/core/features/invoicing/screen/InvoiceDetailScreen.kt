package net.metalbrain.paysmart.core.features.invoicing.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceDetailUiState
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceDetailViewModel

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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.invoice_detail_number, invoice.invoiceNumber),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { Text(text = stringResource(R.string.invoice_detail_status, invoice.status)) }
            item { Text(text = stringResource(R.string.invoice_detail_venue, invoice.venueName)) }
            item {
                Text(
                    text = stringResource(R.string.invoice_detail_period, invoice.weekly.invoiceDate, invoice.weekEndingDate)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.invoice_detail_hours_rate, invoice.totalHours.toString(), invoice.hourlyRate.toString(), invoice.currency)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.invoice_detail_subtotal, formatMoney(invoice.subtotalMinor, invoice.currency))
                )
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
                Text(
                    text = stringResource(R.string.invoice_detail_profile_name, invoice.profile.fullName)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.invoice_detail_profile_email, invoice.profile.email)
                )
            }
        }
    }
}

private fun formatMoney(minor: Int, currency: String): String {
    val major = minor / 100.0
    return String.format(java.util.Locale.US, "%.2f %s", major, currency)
}
