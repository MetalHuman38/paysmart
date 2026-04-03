package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "funding_accounts")
data class FundingAccountEntity(
    @PrimaryKey val userId: String,
    val accountId: String,
    val provider: String,
    val currency: String,
    val accountNumber: String,
    val bankName: String,
    val accountName: String,
    val reference: String,
    val status: String,
    val providerStatus: String,
    val customerId: String,
    val note: String?,
    val createdAtMs: Long,
    val updatedAtMs: Long
)
