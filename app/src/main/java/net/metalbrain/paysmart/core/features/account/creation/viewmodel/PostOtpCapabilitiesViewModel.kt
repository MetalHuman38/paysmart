package net.metalbrain.paysmart.core.features.account.creation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.core.features.capabilities.repository.CountryCapabilityRepository
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2

data class PostOtpCapabilitiesUiState(
    val profile: CountryCapabilityProfile = CountryCapabilityCatalog.defaultProfile()
)

@HiltViewModel
class PostOtpCapabilitiesViewModel @Inject constructor(
    private val countryCapabilityRepository: CountryCapabilityRepository
) : ViewModel() {
    private val countryIso2 = MutableStateFlow(CountryCapabilityCatalog.DEFAULT_ISO2)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = countryIso2
        .flatMapLatest { iso2 ->
            countryCapabilityRepository.observeProfile(iso2)
        }
        .map { profile ->
            PostOtpCapabilitiesUiState(profile = profile)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PostOtpCapabilitiesUiState()
        )

    fun bindCountry(iso2: String) {
        countryIso2.value = normalizeCountryIso2(
            rawIso2 = iso2,
            fallbackIso2 = CountryCapabilityCatalog.DEFAULT_ISO2
        )
    }
}
