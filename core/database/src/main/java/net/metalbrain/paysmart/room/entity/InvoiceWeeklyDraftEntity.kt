package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_weekly_draft")
data class InvoiceWeeklyDraftEntity(
    @PrimaryKey val userId: String,
    val selectedVenueId: String,
    val invoiceDate: String,
    val weekEndingDate: String,
    val shiftsJson: String,
    val hourlyRateInput: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)
