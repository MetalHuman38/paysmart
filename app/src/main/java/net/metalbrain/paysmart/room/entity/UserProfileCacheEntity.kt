package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile_cache")
data class UserProfileCacheEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val photoURL: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val launchInterest: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
