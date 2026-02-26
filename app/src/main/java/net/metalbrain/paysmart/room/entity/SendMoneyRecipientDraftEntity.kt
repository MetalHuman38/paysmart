package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "send_money_recipient_draft")
data class SendMoneyRecipientDraftEntity(
    @PrimaryKey val userId: String,
    val selectedMethod: String,
    val step: String,
    val sourceCurrency: String,
    val targetCurrency: String,
    val sourceAmountInput: String,
    val quoteMethod: String,
    val quotePayloadJson: String?,
    val quoteDataSource: String?,
    val voltTag: String,
    val lookupEmail: String,
    val lookupMobile: String,
    val lookupNote: String,
    val bankFullName: String,
    val bankIban: String,
    val bankBic: String,
    val bankSwift: String,
    val bankName: String,
    val bankAddress: String,
    val bankCity: String,
    val bankCountry: String,
    val bankPostalCode: String,
    val documentFileRef: String,
    val documentType: String,
    val documentNote: String,
    val requestEmail: String,
    val requestFullName: String,
    val requestNote: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)
