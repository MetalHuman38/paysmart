package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_venues",
    indices = [Index(value = ["userId"])]
)
data class InvoiceVenueEntity(
    @PrimaryKey val venueId: String,
    val userId: String,
    val venueName: String,
    val venueAddress: String,
    val defaultHourlyRateInput: String,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

