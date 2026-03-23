package net.metalbrain.paysmart.core.features.account.creation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.core.features.capabilities.repository.CountryCapabilityRepository
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.domain.model.LaunchInterest
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2

data class PostOtpCapabilitiesUiState(
    val profile: CountryCapabilityProfile = CountryCapabilityCatalog.defaultProfile(),
    val selectedInterest: LaunchInterest = LaunchInterest.defaultForCountry(
        CountryCapabilityCatalog.DEFAULT_ISO2
    ),
    val isPersistingSelection: Boolean = false
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class PostOtpCapabilitiesViewModel @Inject constructor(
    private val countryCapabilityRepository: CountryCapabilityRepository,
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userProfileCacheRepository: UserProfileCacheRepository
) : ViewModel() {
    private val countryIso2 = MutableStateFlow(CountryCapabilityCatalog.DEFAULT_ISO2)
    private val selectedInterest = MutableStateFlow<LaunchInterest?>(null)
    private val isPersistingSelection = MutableStateFlow(false)

    val uiState = combine(
        countryIso2
            .flatMapLatest { iso2 ->
                countryCapabilityRepository.observeProfile(iso2)
            },
        selectedInterest,
        isPersistingSelection
    ) { profile, interest, isPersisting ->
        PostOtpCapabilitiesUiState(
            profile = profile,
            selectedInterest = interest ?: LaunchInterest.defaultForCountry(profile.iso2),
            isPersistingSelection = isPersisting
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PostOtpCapabilitiesUiState()
        )

    init {
        viewModelScope.launch {
            val uid = authRepository.currentUser?.uid ?: return@launch
            val cached = userProfileCacheRepository.observeByUid(uid).first()
            cached?.launchInterest?.let { selectedInterest.value = it }
        }
    }

    fun bindCountry(iso2: String) {
        countryIso2.value = normalizeCountryIso2(
            rawIso2 = iso2,
            fallbackIso2 = CountryCapabilityCatalog.DEFAULT_ISO2
        )
    }

    fun selectInterest(launchInterest: LaunchInterest) {
        selectedInterest.value = launchInterest
    }

    fun persistSelection(onPersisted: () -> Unit) {
        viewModelScope.launch {
            val uid = authRepository.currentUser?.uid ?: run {
                onPersisted()
                return@launch
            }
            val interest = selectedInterest.value
                ?: LaunchInterest.defaultForCountry(countryIso2.value)

            isPersistingSelection.value = true
            runCatching {
                userProfileRepository.updateLaunchInterest(uid, interest)
                userProfileCacheRepository.updateLaunchInterest(uid, interest)
            }
            isPersistingSelection.value = false
            onPersisted()
        }
    }
}
