package net.metalbrain.paysmart.core.features.invoicing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.invoicing.data.InvoicePdfRepository
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceDetail
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceReadRepository

@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    private val readRepository: InvoiceReadRepository,
    private val pdfRepository: InvoicePdfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceDetailUiState())
    val uiState: StateFlow<InvoiceDetailUiState> = _uiState.asStateFlow()
    private var loadedInvoiceId: String? = null
    private var pdfPollingJob: Job? = null
    private var autoPrepareInvoiceId: String? = null

    fun load(invoiceId: String) {
        val cleanInvoiceId = invoiceId.trim()
        if (cleanInvoiceId.isBlank() || loadedInvoiceId == cleanInvoiceId) return
        loadedInvoiceId = cleanInvoiceId
        autoPrepareInvoiceId = null
        _uiState.update { it.copy(isLoading = true, error = null, infoMessage = null, shareUri = null) }
        refreshInvoice(cleanInvoiceId, allowAutoPrepare = true)
    }

    fun preparePdf() {
        val invoiceId = _uiState.value.invoice?.invoiceId ?: return
        queuePdf(invoiceId, announceReady = true)
    }

    fun downloadPdf() {
        val invoice = _uiState.value.invoice ?: return
        if (invoice.pdf.status != "ready") {
            _uiState.update { it.copy(error = "Invoice PDF is not ready yet") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingPdf = true, error = null, infoMessage = null) }
            pdfRepository.enqueueSystemDownload(invoice.invoiceId, invoice.pdf.fileName)
                .onSuccess { invoice ->
                    _uiState.update {
                        it.copy(
                            isDownloadingPdf = false,
                            infoMessage = "Invoice PDF download started"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDownloadingPdf = false,
                            error = error.localizedMessage ?: "Unable to download invoice PDF"
                        )
                    }
                }
        }
    }

    fun sharePdf() {
        val invoice = _uiState.value.invoice ?: return
        if (invoice.pdf.status != "ready") {
            _uiState.update { it.copy(error = "Invoice PDF is not ready yet") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSharingPdf = true, error = null, infoMessage = null) }
            pdfRepository.downloadToShareCache(invoice.invoiceId, invoice.pdf.fileName)
                .onSuccess { uri ->
                    _uiState.update {
                        it.copy(
                            isSharingPdf = false,
                            shareUri = uri
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSharingPdf = false,
                            error = error.localizedMessage ?: "Unable to share invoice PDF"
                        )
                    }
                }
        }
    }

    fun consumeShareUri() {
        _uiState.update { it.copy(shareUri = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(error = null, infoMessage = null) }
    }

    private fun refreshInvoice(invoiceId: String, allowAutoPrepare: Boolean) {
        viewModelScope.launch {
            readRepository.getById(invoiceId)
                .onSuccess { invoice ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            invoice = invoice,
                            error = null
                        )
                    }
                    onInvoiceLoaded(invoice, allowAutoPrepare)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            invoice = null,
                            error = error.localizedMessage ?: "Unable to load invoice detail"
                        )
                    }
                }
        }
    }

    private fun onInvoiceLoaded(invoice: InvoiceDetail, allowAutoPrepare: Boolean) {
        when (invoice.pdf.status) {
            "ready", "failed", "not_requested" -> stopPolling()
        }

        if (allowAutoPrepare &&
            autoPrepareInvoiceId != invoice.invoiceId &&
            (invoice.pdf.status == "not_requested" || invoice.pdf.status == "failed")
        ) {
            autoPrepareInvoiceId = invoice.invoiceId
            queuePdf(invoice.invoiceId, announceReady = false)
            return
        }

        if (invoice.pdf.status == "queued" || invoice.pdf.status == "processing") {
            startPolling(invoice.invoiceId)
        }
    }

    private fun queuePdf(invoiceId: String, announceReady: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPreparingPdf = true, error = null, infoMessage = null) }
            pdfRepository.queueGeneration(invoiceId)
                .onSuccess { pdf ->
                    _uiState.update { state ->
                        val invoice = state.invoice?.takeIf { it.invoiceId == invoiceId }?.copy(pdf = pdf)
                        state.copy(
                            isPreparingPdf = false,
                            invoice = invoice ?: state.invoice,
                            infoMessage = if (announceReady && pdf.status == "ready") {
                                "Invoice PDF is ready"
                            } else if (pdf.status == "queued" || pdf.status == "processing") {
                                "Preparing invoice PDF…"
                            } else {
                                state.infoMessage
                            }
                        )
                    }
                    if (pdf.status == "queued" || pdf.status == "processing") {
                        startPolling(invoiceId)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPreparingPdf = false,
                            error = error.localizedMessage ?: "Unable to prepare invoice PDF"
                        )
                    }
                }
        }
    }

    private fun startPolling(invoiceId: String) {
        if (pdfPollingJob?.isActive == true) return
        pdfPollingJob = viewModelScope.launch {
            repeat(8) { attempt ->
                if (attempt > 0) {
                    delay(1_500)
                }
                val result = readRepository.getById(invoiceId)
                result.onSuccess { invoice ->
                    _uiState.update {
                        it.copy(
                            invoice = invoice,
                            error = null,
                            infoMessage = if (invoice.pdf.status == "ready") {
                                "Invoice PDF is ready"
                            } else {
                                it.infoMessage
                            }
                        )
                    }
                    if (invoice.pdf.status == "ready" || invoice.pdf.status == "failed") {
                        stopPolling()
                        return@launch
                    }
                }.onFailure {
                    stopPolling()
                    return@launch
                }
            }
            stopPolling()
        }
    }

    private fun stopPolling() {
        pdfPollingJob?.cancel()
        pdfPollingJob = null
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }
}
