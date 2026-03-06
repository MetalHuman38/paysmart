package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_profile_draft")
data class InvoiceProfileDraftEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val address: String,
    val badgeNumber: String,
    val badgeExpiryDate: String,
    val utrNumber: String,
    val email: String,
    val contactPhone: String,
    val paymentMethod: String,
    val accountNumber: String,
    val sortCode: String,
    val paymentInstructions: String,
    val defaultHourlyRateInput: String,
    val declaration: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)

