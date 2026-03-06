package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SecondaryButton

@Composable
fun InvoicePdfActionsSection(
    status: String,
    error: String?,
    isPreparing: Boolean,
    isDownloading: Boolean,
    isSharing: Boolean,
    onPreparePdf: () -> Unit,
    onDownloadPdf: () -> Unit,
    onSharePdf: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.invoice_detail_pdf_status, status.replace('_', ' '))
        )
        error?.takeIf { it.isNotBlank() }?.let {
            Text(text = stringResource(R.string.invoice_detail_pdf_error, it))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PrimaryButton(
                text = if (isPreparing) {
                    stringResource(R.string.invoice_detail_pdf_preparing)
                } else {
                    stringResource(R.string.invoice_detail_pdf_prepare_action)
                },
                onClick = onPreparePdf,
                enabled = !isPreparing,
                modifier = Modifier.weight(1f)
            )
            SecondaryButton(
                text = if (isDownloading) {
                    stringResource(R.string.invoice_detail_pdf_downloading)
                } else {
                    stringResource(R.string.invoice_detail_pdf_download_action)
                },
                onClick = onDownloadPdf,
                enabled = status == "ready" && !isDownloading,
                modifier = Modifier.weight(1f)
            )
        }
        SecondaryButton(
            text = if (isSharing) {
                stringResource(R.string.invoice_detail_pdf_sharing)
            } else {
                stringResource(R.string.invoice_detail_pdf_share_action)
            },
            onClick = onSharePdf,
            enabled = status == "ready" && !isSharing,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
