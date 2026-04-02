package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "send_money_recent_recipient",
    primaryKeys = ["userId", "recipientKey"],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "updatedAtMs"])
    ]
)
data class SendMoneyRecentRecipientEntity(
    val userId: String,
    val recipientKey: String,
    val selectedMethod: String,
    val sourceCurrency: String,
    val targetCurrency: String,
    val displayName: String,
    val subtitle: String,
    val voltTag: String,
    val lookupEmail: String,
    val lookupMobile: String,
    val bankFullName: String,
    val bankIban: String,
    val bankBic: String,
    val bankSwift: String,
    val bankName: String,
    val bankAddress: String,
    val bankCity: String,
    val bankCountry: String,
    val bankPostalCode: String,
    val requestEmail: String,
    val requestFullName: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)
