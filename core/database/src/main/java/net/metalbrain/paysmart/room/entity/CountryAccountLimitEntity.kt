package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_limits_properties")
data class CountryAccountLimitEntity(
    @PrimaryKey val iso2: String,

    val tabsJson: String,
    val sectionsJson: String,

    val catalogVersion: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)
