package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "managed_cards",
    indices = [
        Index(value = ["userId"])
    ],
    primaryKeys = ["userId", "id"]
)
data class ManagedCardEntity(
    val userId: String,
    val id: String,
    val provider: String,
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val funding: String?,
    val country: String?,
    val fingerprint: String?,
    val isDefault: Boolean,
    val status: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)
