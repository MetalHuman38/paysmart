package net.metalbrain.paysmart.core.features.invoicing.viewmodel

import android.net.Uri
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceDetail

data class InvoiceDetailUiState(
    val isLoading: Boolean = true,
    val isPreparingPdf: Boolean = false,
    val isDownloadingPdf: Boolean = false,
    val isSharingPdf: Boolean = false,
    val invoice: InvoiceDetail? = null,
    val error: String? = null,
    val infoMessage: String? = null,
    val shareUri: Uri? = null
)
