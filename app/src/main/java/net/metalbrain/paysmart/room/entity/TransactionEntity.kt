package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "transactions",
    primaryKeys = ["userId", "id"],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "createdAtMs"])
    ]
)
data class TransactionEntity(
    val userId: String,
    val id: String,
    val title: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val iconRes: Int,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val provider: String?,
    val paymentRail: String?,
    val reference: String,
    val externalReference: String?,
    val statusTimelineJson: String
)
