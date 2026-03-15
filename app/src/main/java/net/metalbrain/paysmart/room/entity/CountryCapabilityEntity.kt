package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "country_capability_catalog")
data class CountryCapabilityEntity(
    @PrimaryKey val iso2: String,
    val countryName: String,
    val flagEmoji: String,
    val currencyCode: String,
    val addMoneyProvidersJson: String,
    val addMoneyMethodsJson: String,
    val capabilitiesJson: String,
    val catalogVersion: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)
