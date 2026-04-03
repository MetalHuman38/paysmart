package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "secure_tokens")
data class SecureTokenEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis()
)
