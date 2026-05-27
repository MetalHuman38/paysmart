package net.metalbrain.paysmart.core.features.invoicing.domain

/**
 * Represents a draft entry for a single work shift within an invoice.
 *
 * @property workDate The date on which the shift occurred.
 * @property dayLabel The display label for the shift row.
 * @property hoursInput The raw user input representing the number of hours worked.
 */
data class InvoiceShiftDraft(
    val workDate: String = "",
    val dayLabel: String = "",
    val hoursInput: String = ""
) {
    fun normalized(): InvoiceShiftDraft {
        return copy(
            workDate = workDate.trim(),
            dayLabel = dayLabel.trim(),
            hoursInput = hoursInput.trim()
        )
    }
}

data class InvoiceWeeklyDraft(
    val selectedVenueId: String = "",
    val invoiceDate: String = "",
    val weekEndingDate: String = "",
    val shifts: List<InvoiceShiftDraft> = defaultShiftRows(),
    val hourlyRateInput: String = "",
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    val totalHours: Double
        get() = shifts.sumOf { it.hoursInput.toDoubleOrNull() ?: 0.0 }

    val billableShifts: List<InvoiceShiftDraft>
        get() = shifts.map { it.normalized() }.filter { shift ->
            (shift.hoursInput.toDoubleOrNull() ?: 0.0) > 0.0
        }

    fun normalized(nowMs: Long = System.currentTimeMillis()): InvoiceWeeklyDraft {
        return copy(
            selectedVenueId = selectedVenueId.trim(),
            invoiceDate = invoiceDate.trim(),
            weekEndingDate = weekEndingDate.trim(),
            shifts = shifts.map { it.normalized() },
            hourlyRateInput = hourlyRateInput.trim(),
            updatedAtMs = nowMs
        )
    }

    fun withVisibleShifts(): InvoiceWeeklyDraft {
        val normalizedShifts = shifts.map { it.normalized() }
        return copy(
            shifts = normalizedShifts.ifEmpty { defaultShiftRows() }
        )
    }

    fun withBillableShiftsOnly(): InvoiceWeeklyDraft {
        return copy(shifts = billableShifts)
    }

    companion object {
        fun defaultShiftRows(): List<InvoiceShiftDraft> {
            return listOf(InvoiceShiftDraft(dayLabel = "Shift 1"))
        }
    }
}
