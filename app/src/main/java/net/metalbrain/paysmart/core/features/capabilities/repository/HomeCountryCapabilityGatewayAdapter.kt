package net.metalbrain.paysmart.core.features.capabilities.repository

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.ui.home.data.HomeCountryCapabilityGateway

@Singleton
class HomeCountryCapabilityGatewayAdapter @Inject constructor(
    private val countryCapabilityRepository: CountryCapabilityRepository,
) : HomeCountryCapabilityGateway {

    override fun observeProfile(countryHint: String?): Flow<CountryCapabilityProfile> {
        return countryCapabilityRepository.observeProfile(countryHint)
    }
}
