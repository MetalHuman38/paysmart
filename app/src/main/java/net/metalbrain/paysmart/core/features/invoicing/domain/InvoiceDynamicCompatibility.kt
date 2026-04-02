package net.metalbrain.paysmart.core.features.invoicing.domain

import java.util.Locale
import net.metalbrain.paysmart.core.invoice.calculation.InvoiceCalculations
import net.metalbrain.paysmart.core.invoice.model.Invoice
import net.metalbrain.paysmart.core.invoice.model.InvoiceFieldKeys
import net.metalbrain.paysmart.core.invoice.model.InvoiceSection
import net.metalbrain.paysmart.core.invoice.model.LineItem
import net.metalbrain.paysmart.core.invoice.model.Totals
import net.metalbrain.paysmart.core.invoice.template.InvoiceTemplateCatalog

internal fun InvoiceWeeklyDraft.toDynamicInvoice(
    profile: InvoiceProfileDraft,
    venue: InvoiceVenueDraft?
): Invoice {
    val template = requireNotNull(
        InvoiceTemplateCatalog.template("weekly_shift_worker_template")
    )

    val sections = template.sections.map { section ->
        when (section.id) {
            "invoice_info" -> section.withFieldValues(
                InvoiceFieldKeys.INVOICE_DATE to invoiceDate,
                InvoiceFieldKeys.WEEK_ENDING to weekEndingDate
            )

            "worker_details" -> section.withFieldValues(
                InvoiceFieldKeys.WORKER_NAME to profile.fullName,
                InvoiceFieldKeys.WORKER_ADDRESS to profile.address,
                InvoiceFieldKeys.WORKER_BADGE_NUMBER to profile.badgeNumber,
                InvoiceFieldKeys.WORKER_BADGE_EXPIRY to profile.badgeExpiryDate,
                InvoiceFieldKeys.WORKER_UTR to profile.utrNumber,
                InvoiceFieldKeys.WORKER_EMAIL to profile.email,
                InvoiceFieldKeys.WORKER_PHONE to profile.contactPhone,
                InvoiceFieldKeys.PAYMENT_ACCOUNT_NUMBER to profile.accountNumber,
                InvoiceFieldKeys.PAYMENT_SORT_CODE to profile.sortCode,
                InvoiceFieldKeys.PAYMENT_INSTRUCTIONS to profile.paymentInstructions,
                InvoiceFieldKeys.DEFAULT_RATE to profile.defaultHourlyRateInput.toDoubleOrNull()
            )

            "client_details" -> section.withFieldValues(
                InvoiceFieldKeys.CLIENT_NAME to venue?.venueName.orEmpty(),
                InvoiceFieldKeys.CLIENT_ADDRESS to venue?.venueAddress.orEmpty(),
                InvoiceFieldKeys.CLIENT_COUNTRY to "GB"
            )

            else -> section
        }
    }

    val rateValue = hourlyRateInput.toDoubleOrNull() ?: 0.0
    val lineItems = shifts.mapIndexed { index, shift ->
        LineItem(
            id = "legacy_shift_$index",
            fields = template.lineItemFields.map { field ->
                when (field.key) {
                    InvoiceFieldKeys.LINE_DATE -> field.copy(value = shift.workDate)
                    InvoiceFieldKeys.LINE_HOURS -> field.copy(value = shift.hoursInput.toDoubleOrNull())
                    InvoiceFieldKeys.LINE_RATE -> field.copy(value = rateValue)
                    InvoiceFieldKeys.LINE_AMOUNT -> field.copy(
                        value = (shift.hoursInput.toDoubleOrNull() ?: 0.0) * rateValue
                    )
                    else -> field
                }
            }
        )
    }

    val invoice = Invoice(
        id = "weekly:${selectedVenueId.ifBlank { "draft" }}:${weekEndingDate.ifBlank { "unscheduled" }}",
        templateId = template.id,
        professionId = "zero_hours_worker",
        sections = sections,
        lineItems = InvoiceCalculations.withComputedAmounts(lineItems),
        totals = Totals(currencyCode = "GBP")
    )
    return InvoiceCalculations.recalculate(invoice)
}

internal fun Invoice.toLegacyWeeklyDraft(
    fallbackSelectedVenueId: String = ""
): InvoiceWeeklyDraft {
    val invoiceDate = findSectionValue("invoice_info", InvoiceFieldKeys.INVOICE_DATE)
    val weekEndingDate = findSectionValue("invoice_info", InvoiceFieldKeys.WEEK_ENDING)
    val hourlyRate = lineItems.firstOrNull()
        ?.fields
        ?.firstOrNull { it.key == InvoiceFieldKeys.LINE_RATE }
        ?.value
        ?.toNumericString()
        .orEmpty()

    return InvoiceWeeklyDraft(
        selectedVenueId = fallbackSelectedVenueId,
        invoiceDate = invoiceDate,
        weekEndingDate = weekEndingDate,
        shifts = lineItems.mapIndexed { index, lineItem ->
            InvoiceShiftDraft(
                workDate = lineItem.fields.firstOrNull { it.key == InvoiceFieldKeys.LINE_DATE }
                    ?.value
                    ?.toString()
                    .orEmpty(),
                dayLabel = LEGACY_WEEKDAY_LABELS.getOrElse(index) { "Shift ${index + 1}" },
                hoursInput = lineItem.fields.firstOrNull { it.key == InvoiceFieldKeys.LINE_HOURS }
                    ?.value
                    ?.toNumericString()
                    .orEmpty()
            )
        }.ifEmpty { InvoiceWeeklyDraft.defaultWeekShifts() },
        hourlyRateInput = hourlyRate
    ).withFullWeek()
}

internal fun Invoice.toLegacyProfileDraft(): InvoiceProfileDraft {
    return InvoiceProfileDraft(
        fullName = findSectionValue("worker_details", InvoiceFieldKeys.WORKER_NAME),
        address = findSectionValue("worker_details", InvoiceFieldKeys.WORKER_ADDRESS),
        badgeNumber = findSectionValue("worker_details", InvoiceFieldKeys.WORKER_BADGE_NUMBER),
        badgeExpiryDate = findSectionValue("worker_details", InvoiceFieldKeys.WORKER_BADGE_EXPIRY),
        utrNumber = findSectionValue("worker_details", InvoiceFieldKeys.WORKER_UTR),
        email = findSectionValue("worker_details", InvoiceFieldKeys.WORKER_EMAIL),
        contactPhone = findSectionValue("worker_details", InvoiceFieldKeys.WORKER_PHONE),
        accountNumber = findSectionValue("worker_details", InvoiceFieldKeys.PAYMENT_ACCOUNT_NUMBER),
        sortCode = findSectionValue("worker_details", InvoiceFieldKeys.PAYMENT_SORT_CODE),
        paymentInstructions = findSectionValue("worker_details", InvoiceFieldKeys.PAYMENT_INSTRUCTIONS),
        defaultHourlyRateInput = findSectionValue("worker_details", InvoiceFieldKeys.DEFAULT_RATE)
    ).normalized()
}

internal fun Invoice.toLegacyVenueDraft(
    venueId: String = "venue_migrated"
): InvoiceVenueDraft {
    return InvoiceVenueDraft(
        venueId = venueId,
        venueName = findSectionValue("client_details", InvoiceFieldKeys.CLIENT_NAME),
        venueAddress = findSectionValue("client_details", InvoiceFieldKeys.CLIENT_ADDRESS),
        defaultHourlyRateInput = lineItems.firstOrNull()
            ?.fields
            ?.firstOrNull { it.key == InvoiceFieldKeys.LINE_RATE }
            ?.value
            ?.toNumericString()
            .orEmpty()
    ).normalized()
}

private fun InvoiceSection.withFieldValues(vararg values: Pair<String, Any?>): InvoiceSection {
    val lookup = values.toMap()
    return copy(
        fields = fields.map { field ->
            if (lookup.containsKey(field.key)) {
                field.copy(value = lookup[field.key])
            } else {
                field
            }
        }
    )
}

private fun Invoice.findSectionValue(sectionId: String, key: String): String {
    return sections.firstOrNull { it.id == sectionId }
        ?.fields
        ?.firstOrNull { it.key == key }
        ?.value
        ?.toString()
        .orEmpty()
}

private fun Any?.toNumericString(): String {
    return when (this) {
        is String -> this
        is Int -> this.toString()
        is Long -> this.toString()
        is Float -> this.toPlainNumberString()
        is Double -> this.toPlainNumberString()
        else -> ""
    }
}

private fun Double.toPlainNumberString(): String {
    return if (this % 1.0 == 0.0) {
        this.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
    }
}

private fun Float.toPlainNumberString(): String = this.toDouble().toPlainNumberString()

private val LEGACY_WEEKDAY_LABELS = listOf(
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
    "Sunday"
)
