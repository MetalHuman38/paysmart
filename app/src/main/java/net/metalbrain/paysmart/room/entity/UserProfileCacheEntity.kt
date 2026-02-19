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
    val updatedAt: Long = System.currentTimeMillis()
)
