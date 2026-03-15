package net.metalbrain.paysmart.core.features.fundingaccount.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.repository.CountryCapabilityRepository
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountData
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountErrorCode
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountStatus
import net.metalbrain.paysmart.core.features.fundingaccount.repository.FundingAccountApiException
import net.metalbrain.paysmart.core.features.fundingaccount.repository.FundingAccountGateway
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountScreenPhase
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountUiState
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class FundingAccountViewModel @Inject constructor(
    private val fundingAccountGateway: FundingAccountGateway,
    private val profileCacheRepository: UserProfileCacheRepository,
    private val countryCapabilityRepository: CountryCapabilityRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val syncState = MutableStateFlow(FundingAccountSyncState())

    private val capabilityProfile = userManager.authState
        .flatMapLatest { authState ->
            when (authState) {
                is AuthState.Authenticated -> profileCacheRepository.observeByUid(authState.uid)
                    .flatMapLatest { profile ->
                        countryCapabilityRepository.observeProfile(profile?.country)
                    }

                else -> countryCapabilityRepository.observeProfile(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CountryCapabilityCatalog.defaultProfile()
        )

    private val currentAccount = fundingAccountGateway.observeCurrent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val uiState = combine(currentAccount, capabilityProfile, syncState) { account, profile, sync ->
        val supportsReceiveMoney = profile.capabilities.any { item ->
            item.key == CapabilityKey.RECEIVE_MONEY
        }
        val supportsAccountTransfer = profile.addMoneyMethods.contains(FxPaymentMethod.ACCOUNT_TRANSFER)
        val marketSupported = supportsReceiveMoney && supportsAccountTransfer

        FundingAccountUiState(
            account = account,
            phase = derivePhase(
                account = account,
                isMarketSupported = marketSupported,
                sync = sync
            ),
            isInitialLoading = sync.isInitialLoading,
            isRefreshing = sync.isRefreshing,
            isProvisioning = sync.isProvisioning,
            isMarketSupported = marketSupported,
            countryIso2 = profile.iso2,
            countryName = profile.countryName,
            countryFlagEmoji = profile.flagEmoji,
            currencyCode = profile.currencyCode,
            provider = account?.provider ?: "flutterwave",
            lastErrorCode = sync.lastErrorCode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FundingAccountUiState()
    )

    init {
        viewModelScope.launch {
            userManager.authState.collect { authState ->
                if (authState is AuthState.Authenticated) {
                    sync(isInitial = true)
                } else {
                    syncState.value = FundingAccountSyncState(isInitialLoading = false)
                }
            }
        }
    }

    fun refresh() {
        sync(isInitial = false)
    }

    fun provision() {
        val state = uiState.value
        if (!state.isMarketSupported || state.isProvisioning) return

        viewModelScope.launch {
            syncState.update {
                it.copy(
                    isInitialLoading = false,
                    isRefreshing = false,
                    isProvisioning = true,
                    lastErrorCode = null
                )
            }

            fundingAccountGateway.provision().fold(
                onSuccess = {
                    syncState.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            isProvisioning = false,
                            lastErrorCode = null
                        )
                    }
                },
                onFailure = { throwable ->
                    syncState.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            isProvisioning = false,
                            lastErrorCode = throwable.toFundingAccountErrorCode()
                        )
                    }
                }
            )
        }
    }

    private fun sync(isInitial: Boolean) {
        viewModelScope.launch {
            syncState.update { current ->
                current.copy(
                    isInitialLoading = isInitial && currentAccount.value == null,
                    isRefreshing = !isInitial,
                    isProvisioning = false,
                    lastErrorCode = null
                )
            }

            fundingAccountGateway.syncFromServer().fold(
                onSuccess = {
                    syncState.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            isProvisioning = false,
                            lastErrorCode = null
                        )
                    }
                },
                onFailure = { throwable ->
                    syncState.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            isProvisioning = false,
                            lastErrorCode = throwable.toFundingAccountErrorCode()
                        )
                    }
                }
            )
        }
    }
}

private fun derivePhase(
    account: FundingAccountData?,
    isMarketSupported: Boolean,
    sync: FundingAccountSyncState
): FundingAccountScreenPhase {
    if (sync.isInitialLoading && account == null) {
        return FundingAccountScreenPhase.LOADING
    }

    if (!isMarketSupported) {
        return FundingAccountScreenPhase.UNSUPPORTED_MARKET
    }

    if (account != null) {
        return when (account.status) {
            FundingAccountStatus.ACTIVE -> FundingAccountScreenPhase.READY
            FundingAccountStatus.PENDING -> FundingAccountScreenPhase.PENDING
            FundingAccountStatus.DISABLED,
            FundingAccountStatus.FAILED -> FundingAccountScreenPhase.ERROR
        }
    }

    return when (sync.lastErrorCode) {
        FundingAccountErrorCode.FLUTTERWAVE_FUNDING_ACCOUNT_KYC_REQUIRED ->
            FundingAccountScreenPhase.KYC_REQUIRED

        null -> FundingAccountScreenPhase.EMPTY
        else -> FundingAccountScreenPhase.ERROR
    }
}

private fun Throwable.toFundingAccountErrorCode(): FundingAccountErrorCode {
    return (this as? FundingAccountApiException)?.code
        ?: FundingAccountErrorCode.FLUTTERWAVE_PROVIDER_REJECTED
}

private data class FundingAccountSyncState(
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isProvisioning: Boolean = false,
    val lastErrorCode: FundingAccountErrorCode? = null
)
