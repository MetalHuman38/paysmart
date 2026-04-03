package net.metalbrain.paysmart.ui.home.data

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile

interface HomeCountryCapabilityGateway {
    fun observeProfile(countryHint: String?): Flow<CountryCapabilityProfile>
}
