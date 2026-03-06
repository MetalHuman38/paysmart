package net.metalbrain.paysmart.core.features.invoicing.domain

/**
 * Represents a draft entry for a single work shift within an invoice.
 *
 * @property workDate The date on which the shift occurred.
 * @property dayLabel The display label for the day of the week (e.g., "Monday").
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

private val WEEKDAY_LABELS: List<String> = listOf(
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
    "Sunday"
)

data class InvoiceWeeklyDraft(
    val selectedVenueId: String = "",
    val invoiceDate: String = "",
    val weekEndingDate: String = "",
    val shifts: List<InvoiceShiftDraft> = defaultWeekShifts(),
    val hourlyRateInput: String = "",
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    val totalHours: Double
        get() = shifts.sumOf { it.hoursInput.toDoubleOrNull() ?: 0.0 }

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

    fun withFullWeek(): InvoiceWeeklyDraft {
        val byDayKey = shifts
            .map { it.normalized() }
            .associateBy { weekdayKey(it.dayLabel) }

        return copy(
            shifts = WEEKDAY_LABELS.map { label ->
                val existing = byDayKey[weekdayKey(label)]
                existing?.copy(dayLabel = label) ?: InvoiceShiftDraft(dayLabel = label)
            }
        )
    }

    companion object {
        fun defaultWeekShifts(): List<InvoiceShiftDraft> {
            return WEEKDAY_LABELS.map { label ->
                InvoiceShiftDraft(dayLabel = label)
            }
        }
    }
}

private fun weekdayKey(raw: String): String {
    return raw.trim().lowercase().take(3)
}
