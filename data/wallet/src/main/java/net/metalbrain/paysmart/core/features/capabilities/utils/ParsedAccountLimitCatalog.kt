package net.metalbrain.paysmart.core.features.capabilities.utils

import net.metalbrain.paysmart.room.entity.CountryAccountLimitEntity

data class ParsedAccountLimitCatalog(
    val catalogVersion: String,
    val entities: List<CountryAccountLimitEntity>
)
