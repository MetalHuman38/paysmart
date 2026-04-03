package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fx_quote_cache")
data class FxQuoteCacheEntity(
    @PrimaryKey val cacheKey: String,
    val userId: String,
    val sourceCurrency: String,
    val targetCurrency: String,
    val sourceAmount: Double,
    val method: String,
    val rate: Double,
    val recipientAmount: Double,
    val feesJson: String,
    val guaranteeSeconds: Int,
    val arrivalSeconds: Int,
    val rateSource: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)
