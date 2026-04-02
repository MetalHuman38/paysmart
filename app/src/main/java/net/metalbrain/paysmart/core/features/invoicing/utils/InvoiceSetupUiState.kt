package net.metalbrain.paysmart.core.features.invoicing.utils

import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.core.invoice.model.Invoice
import net.metalbrain.paysmart.core.invoice.model.InvoiceField
import net.metalbrain.paysmart.core.invoice.model.InvoiceFieldKeys
import net.metalbrain.paysmart.core.invoice.model.InvoiceFormStep
import net.metalbrain.paysmart.core.invoice.model.InvoiceSection
import net.metalbrain.paysmart.core.invoice.model.LineItem
import net.metalbrain.paysmart.core.invoice.model.doubleValue
import java.util.Locale


fun InvoiceSetupUiState.canAdvanceCurrentStep(): Boolean {
    return when (formStep) {
        InvoiceFormStep.QUICK_START -> selectedProfession != null || selectedTemplate != null
        InvoiceFormStep.INVOICE_INFO -> draftInvoice.sectionOrNull("invoice_info")?.requiredFieldsComplete() ?: false
        InvoiceFormStep.WORKER_DETAILS -> draftInvoice.sectionOrNull("worker_details")?.requiredFieldsComplete() ?: false
        InvoiceFormStep.CLIENT_DETAILS -> effectiveSelectedVenueId.isNotBlank() &&
                (draftInvoice.sectionOrNull("client_details")
                    ?.filteredCopy(ignoredKeys = setOf(InvoiceFieldKeys.CLIENT_NAME, InvoiceFieldKeys.CLIENT_ADDRESS))
                    ?.requiredFieldsComplete() ?: true)

        InvoiceFormStep.WORK_DETAILS -> {
            val extraSectionsReady = draftInvoice.sections
                .filterNot { section -> section.id in setOf("invoice_info", "worker_details", "client_details") }
                .all { section -> section.requiredFieldsComplete() }
            extraSectionsReady && draftInvoice.lineItems.any(LineItem::isReadyForReview)
        }

        InvoiceFormStep.REVIEW -> canFinalize
    }
}

fun Invoice.sectionOrNull(sectionId: String): InvoiceSection? =
    sections.firstOrNull { it.id == sectionId }

fun InvoiceSection.filteredCopy(
    ignoredKeys: Set<String> = emptySet()
): InvoiceSection {
    return copy(fields = fields.filterNot { field -> field.key in ignoredKeys })
}

fun InvoiceSection.filteredForReview(): InvoiceSection {
    return copy(fields = fields.filter { field -> fieldDisplayValue(field).isNotBlank() })
}

fun InvoiceSection.requiredFieldsComplete(): Boolean {
    return fields.filter { field -> field.required }.all(InvoiceField::isCompletedForDisplay)
}

fun LineItem.isReadyForReview(): Boolean {
    val requiredFieldsReady = fields
        .filter { field -> field.required && field.key != InvoiceFieldKeys.LINE_AMOUNT }
        .all(InvoiceField::isCompletedForDisplay)
    val workedHours = fields.firstOrNull { field -> field.key == InvoiceFieldKeys.LINE_HOURS }
        ?.doubleValue()
        ?: 0.0
    return requiredFieldsReady && workedHours > 0.0
}

fun InvoiceSetupUiState.primaryLineRateValue(): String {
    return draftInvoice.lineItems.firstOrNull()
        ?.fields
        ?.firstOrNull { field -> field.key == InvoiceFieldKeys.LINE_RATE }
        ?.let(::fieldDisplayValue)
        .orEmpty()
}

fun InvoiceField.isCompletedForDisplay(): Boolean {
    return when (val raw = value) {
        null -> false
        is String -> raw.trim().isNotBlank()
        else -> true
    }
}

fun InvoiceField.prefersMultiLine(): Boolean {
    return key in setOf(
        InvoiceFieldKeys.WORKER_ADDRESS,
        InvoiceFieldKeys.CLIENT_ADDRESS,
        InvoiceFieldKeys.PAYMENT_INSTRUCTIONS,
        InvoiceFieldKeys.DESCRIPTION,
        InvoiceFieldKeys.INCIDENT_NOTES
    )
}

fun fieldDisplayValue(field: InvoiceField): String {
    return when (val raw = field.value) {
        null -> ""
        is String -> raw
        is Double -> formatDecimal(raw)
        is Float -> formatDecimal(raw.toDouble())
        is Int -> raw.toString()
        is Long -> raw.toString()
        is Boolean -> if (raw) "Yes" else "No"
        else -> raw.toString()
    }
}

fun formatDecimal(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}

fun formatMoneyMinor(amountMinor: Long, currencyCode: String): String {
    return "$currencyCode ${String.format(Locale.US, "%.2f", amountMinor / 100.0)}"
}
