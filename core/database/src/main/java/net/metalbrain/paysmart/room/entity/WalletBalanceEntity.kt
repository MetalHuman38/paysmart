package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_balances")
data class WalletBalanceEntity(
    @PrimaryKey val userId: String,
    val jsonData: String,
    val salt: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
