package net.metalbrain.paysmart.core.features.addmoney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyPaymentSheetCustomer
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyPaymentSheetLaunch
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionStatus
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyUiError
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyUiState
import net.metalbrain.paysmart.core.features.addmoney.repository.AddMoneyRepository
import net.metalbrain.paysmart.core.features.addmoney.util.AddMoneyFailurePhase
import net.metalbrain.paysmart.core.features.addmoney.util.addMoneyUnavailableMessage
import net.metalbrain.paysmart.core.features.addmoney.util.addMoneyUnavailableSupportingText
import net.metalbrain.paysmart.core.features.addmoney.util.resolvePreferredAddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.util.resolveAddMoneyUiError
import net.metalbrain.paysmart.core.features.cards.repository.ManagedCardsGateway
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.data.repository.WalletBalanceGateway
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.core.features.capabilities.repository.CountryCapabilityRepository
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteQuery
import net.metalbrain.paysmart.core.features.fx.repository.FxQuoteRepository
import java.math.BigDecimal
import java.math.RoundingMode

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AddMoneyViewModel @Inject constructor(
    private val addMoneyRepository: AddMoneyRepository,
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val userProfileCacheRepository: UserProfileCacheRepository,
    private val countryCapabilityRepository: CountryCapabilityRepository,
    private val walletBalanceRepository: WalletBalanceGateway,
    private val fxQuoteRepository: FxQuoteRepository,
    private val managedCardsGateway: ManagedCardsGateway
) : ViewModel() {

    
    private val fallbackPublishableKey = Env.fallbackPublishableKey
        .takeIf { it.isNotEmpty() }

    private val _uiState = MutableStateFlow(AddMoneyUiState())
    val uiState: StateFlow<AddMoneyUiState> = _uiState.asStateFlow()
    private var quoteRefreshJob: Job? = null
    private var statusPollingJob: Job? = null

    init {
        observeCountryCapabilities()
        scheduleQuoteRefresh()
    }

    fun onAmountInputChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }.take(12)
        _uiState.update {
            it.copy(
                amountInput = filtered,
                error = null,
                quoteError = null
            )
        }
        scheduleQuoteRefresh()
    }

    fun onCurrencyChanged(value: String) {
        val normalized = value
            .filter { it.isLetter() }
            .uppercase()
            .take(3)
        _uiState.update {
            it.copy(
                currency = normalized.ifBlank { "GBP" },
                error = null,
                quoteError = null
            )
        }
        scheduleQuoteRefresh()
    }

    fun onQuoteTargetCurrencyChanged(value: String) {
        val normalized = value
            .filter { it.isLetter() }
            .uppercase()
            .take(3)
        _uiState.update {
            it.copy(
                quoteTargetCurrency = normalized.ifBlank { "EUR" },
                quoteError = null
            )
        }
        scheduleQuoteRefresh()
    }

    fun onQuoteMethodChanged(method: FxPaymentMethod) {
        if (method !in _uiState.value.availableMethods) {
            _uiState.update {
                it.copy(
                    error = plainUiError("Payment method unavailable for this country")
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                quoteMethod = method,
                error = null,
                quoteError = null
            )
        }
        scheduleQuoteRefresh()
    }

    fun rotateQuoteMethod() {
        val methods = _uiState.value.availableMethods
        if (methods.isEmpty()) return
        if (methods.size == 1) {
            onQuoteMethodChanged(methods.first())
            return
        }
        val current = _uiState.value.quoteMethod
        val currentIndex = methods.indexOf(current).takeIf { it >= 0 } ?: 0
        val next = methods[(currentIndex + 1) % methods.size]
        onQuoteMethodChanged(next)
    }

    fun refreshQuote() {
        val query = buildQuoteQueryOrNull()
        if (query == null) {
            _uiState.update {
                it.copy(
                    quote = null,
                    quoteDataSource = null,
                    quoteError = "Enter a valid amount to fetch a live quote",
                    isQuoteLoading = false
                )
            }
            return
        }

        if (_uiState.value.isQuoteLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isQuoteLoading = true,
                    quoteError = null
                )
            }

            fxQuoteRepository.getQuote(query)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isQuoteLoading = false,
                            quote = result.quote,
                            quoteDataSource = result.dataSource,
                            quoteError = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isQuoteLoading = false,
                            quote = null,
                            quoteDataSource = null,
                            quoteError = error.localizedMessage ?: "Unable to fetch quote"
                        )
                    }
                }
        }
    }

    fun createPaymentSession() {
        if (_uiState.value.isSubmitting) return

        val amountMinor = parseAmountToMinor(_uiState.value.amountInput)
        if (amountMinor == null || amountMinor <= 0) {
            _uiState.update {
                it.copy(error = plainUiError("Enter a valid amount"))
            }
            return
        }

        val currency = _uiState.value.currency.ifBlank { "GBP" }.uppercase()
        val requestedProvider = resolveProviderForRequest()
        if (requestedProvider == null) {
            _uiState.update {
                it.copy(
                    error = AddMoneyUiError(
                        title = "Add money unavailable",
                        message = addMoneyUnavailableMessage(it.countryName),
                        supportingText = addMoneyUnavailableSupportingText(it.countryName)
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    error = null,
                    infoMessage = null,
                    providerActionUrl = null,
                    activeSessionCurrency = null
                )
            }

            addMoneyRepository.createSession(amountMinor, currency, requestedProvider)
                .onSuccess { session ->
                    val resolvedProvider = session.provider
                    val paymentIntentClientSecret = session.paymentIntentClientSecret
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                    val publishableKey = session.publishableKey
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?: fallbackPublishableKey

                    if (resolvedProvider == AddMoneyProvider.STRIPE &&
                        (paymentIntentClientSecret == null || publishableKey == null)
                    ) {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                sessionId = session.sessionId,
                                activeSessionProvider = resolvedProvider,
                                activeSessionCurrency = session.currency,
                                sessionStatus = session.status,
                                error = AddMoneyUiError(
                                    title = "Payment configuration missing",
                                    message = "Payment session is missing PaymentSheet configuration."
                                )
                            )
                        }
                        return@onSuccess
                    }

                    val paymentSheetCustomer = if (resolvedProvider == AddMoneyProvider.STRIPE) {
                        val customerId = session.customerId?.takeIf { it.isNotBlank() }
                        val ephemeralKeySecret = session.customerEphemeralKeySecret
                            ?.takeIf { it.isNotBlank() }
                        if (customerId != null && ephemeralKeySecret != null) {
                            AddMoneyPaymentSheetCustomer(
                                customerId = customerId,
                                ephemeralKeySecret = ephemeralKeySecret,
                                defaultPaymentMethodId = session.defaultPaymentMethodId
                            )
                        } else {
                            null
                        }
                    } else {
                        null
                    }

                    val infoMessage = when (session.status) {
                        AddMoneySessionStatus.SUCCEEDED -> "Payment completed. Refresh to sync wallet."
                        AddMoneySessionStatus.EXPIRED -> "Session expired. Create a new top up session."
                        else -> when (resolvedProvider) {
                            AddMoneyProvider.STRIPE -> "Payment sheet ready."
                            AddMoneyProvider.FLUTTERWAVE ->
                                if (!session.checkoutUrl.isNullOrBlank()) {
                                    "Flutterwave session ready. Continue to secure checkout."
                                } else {
                                    "Flutterwave session created. Make transfer and refresh status."
                                }
                        }
                    }

                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            sessionId = session.sessionId,
                            activeSessionProvider = resolvedProvider,
                            activeSessionMethod = state.quoteMethod,
                            activeSessionCurrency = session.currency,
                            sessionStatus = session.status,
                            paymentSheetLaunch = if (resolvedProvider == AddMoneyProvider.STRIPE) {
                                AddMoneyPaymentSheetLaunch(
                                    publishableKey = publishableKey!!,
                                    paymentIntentClientSecret = paymentIntentClientSecret!!,
                                    customer = paymentSheetCustomer
                                )
                            } else {
                                null
                            },
                            providerActionUrl = session.checkoutUrl,
                            infoMessage = infoMessage
                        )
                    }
                    mirrorSessionStatusToTransactions(session)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = resolveAddMoneyUiError(
                                throwable = error,
                                isLocal = Env.isLocal,
                                phase = AddMoneyFailurePhase.CREATE_SESSION
                            )
                        )
                    }
                }
        }
    }

    fun consumePaymentSheetLaunch() {
        _uiState.update { it.copy(paymentSheetLaunch = null) }
    }

    fun onPaymentSheetCompleted() {
        _uiState.update {
            it.copy(
                infoMessage = "Payment submitted. Finalizing with Stripe...",
                error = null
            )
        }
        refreshSessionStatus()
        startSessionStatusPolling()
    }

    fun onPaymentSheetCanceled() {
        stopSessionStatusPolling()
        _uiState.update {
            it.copy(
                infoMessage = "Payment canceled.",
                error = null
            )
        }
    }

    fun onPaymentSheetFailed(message: String?) {
        stopSessionStatusPolling()
        _uiState.update {
            it.copy(
                error = AddMoneyUiError(
                    title = "Payment error",
                    message = message?.takeIf { value -> value.isNotBlank() }
                        ?: "Payment failed before confirmation"
                ),
                infoMessage = null
            )
        }
    }

    fun refreshSessionStatus() {
        val sessionId = _uiState.value.sessionId
        if (sessionId.isNullOrBlank() || _uiState.value.isCheckingStatus) return
        val provider = _uiState.value.activeSessionProvider ?: resolveProviderForRequest()
        if (provider == null) {
            _uiState.update {
                it.copy(
                    error = AddMoneyUiError(
                        title = "Add money unavailable",
                        message = addMoneyUnavailableMessage(it.countryName),
                        supportingText = addMoneyUnavailableSupportingText(it.countryName)
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCheckingStatus = true,
                    error = null,
                    infoMessage = null
                )
            }

            addMoneyRepository.getSessionStatus(sessionId, provider)
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            isCheckingStatus = false,
                            activeSessionProvider = session.provider,
                            activeSessionCurrency = session.currency,
                            sessionStatus = session.status,
                            providerActionUrl = session.checkoutUrl ?: it.providerActionUrl,
                            infoMessage = when (session.status) {
                                AddMoneySessionStatus.SUCCEEDED -> "Top up completed. Wallet is syncing."
                                AddMoneySessionStatus.PENDING,
                                AddMoneySessionStatus.CREATED -> "Payment still pending."
                                AddMoneySessionStatus.EXPIRED -> "Session expired. Start again."
                                AddMoneySessionStatus.FAILED -> "Payment failed. Try again."
                            }
                        )
                    }
                    mirrorSessionStatusToTransactions(session)

                    if (session.status == AddMoneySessionStatus.SUCCEEDED) {
                        stopSessionStatusPolling()
                        syncWalletSnapshot()
                        syncManagedCards()
                    } else if (session.status == AddMoneySessionStatus.FAILED ||
                        session.status == AddMoneySessionStatus.EXPIRED
                    ) {
                        stopSessionStatusPolling()
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCheckingStatus = false,
                            error = resolveAddMoneyUiError(
                                throwable = error,
                                isLocal = Env.isLocal,
                                phase = AddMoneyFailurePhase.REFRESH_STATUS
                            )
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, quoteError = null) }
    }

    private suspend fun syncWalletSnapshot() {
        val uid = authRepository.currentUser?.uid ?: return
        walletBalanceRepository.syncFromServer(uid)
    }

    private fun syncManagedCards() {
        viewModelScope.launch {
            managedCardsGateway.syncFromServer()
        }
    }

    private fun parseAmountToMinor(raw: String): Int? {
        val normalized = raw.trim()
        if (normalized.isEmpty()) return null

        val amount = normalized.toBigDecimalOrNull() ?: return null
        val minor = amount.multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()
        return minor.takeIf { it > 0 }
    }

    private fun parseAmountToMajor(raw: String): Double? {
        val normalized = raw.trim()
        if (normalized.isEmpty()) return null
        return normalized.toDoubleOrNull()?.takeIf { it > 0.0 }
    }

    private fun scheduleQuoteRefresh() {
        quoteRefreshJob?.cancel()
        quoteRefreshJob = viewModelScope.launch {
            delay(250)
            refreshQuote()
        }
    }

    private fun observeCountryCapabilities() {
        viewModelScope.launch {
            val uid = authRepository.currentUser?.uid
            if (uid.isNullOrBlank()) {
                countryCapabilityRepository.observeProfile(null).collectLatest { profile ->
                    applyCountryCapabilityProfile(profile)
                }
                return@launch
            }
            userProfileCacheRepository.observeByUid(uid)
                .flatMapLatest { profile ->
                    countryCapabilityRepository.observeProfile(profile?.country)
                }
                .collectLatest { profile ->
                    applyCountryCapabilityProfile(profile)
                }
        }
    }

    private fun applyCountryCapabilityProfile(profile: CountryCapabilityProfile) {
        _uiState.update { state ->
            val allowedMethods = profile.addMoneyMethods.ifEmpty { FxPaymentMethod.entries }
            val nextMethod = if (state.quoteMethod in allowedMethods) {
                state.quoteMethod
            } else {
                allowedMethods.first()
            }

            val currentCurrency = state.currency.trim().uppercase()
            val nextCurrency = if (currentCurrency.isBlank() || currentCurrency == "GBP") {
                profile.currencyCode
            } else {
                currentCurrency
            }

            state.copy(
                countryIso2 = profile.iso2,
                countryName = profile.countryName,
                countryFlagEmoji = profile.flagEmoji,
                countryCurrencyCode = profile.currencyCode,
                topUpPolicyHint = CountryCapabilityCatalog.topUpPolicyHint(profile),
                availableAddMoneyProviders = profile.addMoneyProviders,
                availableMethods = allowedMethods,
                countryCapabilities = profile.capabilities,
                quoteMethod = nextMethod,
                currency = nextCurrency
            )
        }
        scheduleQuoteRefresh()
    }

    private fun buildQuoteQueryOrNull(): FxQuoteQuery? {
        val sourceAmount = parseAmountToMajor(_uiState.value.amountInput) ?: return null
        val sourceCurrency = _uiState.value.currency.trim().uppercase()
        val targetCurrency = _uiState.value.quoteTargetCurrency.trim().uppercase()
        if (sourceCurrency.length != 3 || targetCurrency.length != 3) return null

        val selectedMethod = _uiState.value.quoteMethod
        val method = if (selectedMethod in _uiState.value.availableMethods) {
            selectedMethod
        } else {
            _uiState.value.availableMethods.firstOrNull() ?: selectedMethod
        }

        return FxQuoteQuery(
            sourceCurrency = sourceCurrency,
            targetCurrency = targetCurrency,
            sourceAmount = sourceAmount,
            method = method
        )
    }

    private fun mirrorSessionStatusToTransactions(
        session: net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
    ) {
        viewModelScope.launch {
            transactionRepository.recordAddMoneySession(session = session)
        }
    }

    private fun startSessionStatusPolling() {
        val sessionId = _uiState.value.sessionId ?: return
        statusPollingJob?.cancel()
        statusPollingJob = viewModelScope.launch {
            repeat(8) { index ->
                if (index > 0) {
                    delay(1_500)
                }
                refreshSessionStatus()
                val status = _uiState.value.sessionStatus
                if (status == AddMoneySessionStatus.SUCCEEDED ||
                    status == AddMoneySessionStatus.FAILED ||
                    status == AddMoneySessionStatus.EXPIRED ||
                    _uiState.value.sessionId != sessionId
                ) {
                    stopSessionStatusPolling()
                    return@launch
                }
            }
        }
    }

    private fun stopSessionStatusPolling() {
        statusPollingJob?.cancel()
        statusPollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopSessionStatusPolling()
    }

    private fun resolveProviderForRequest(): AddMoneyProvider? {
        return resolvePreferredAddMoneyProvider(_uiState.value.availableAddMoneyProviders)
    }

    private fun plainUiError(message: String): AddMoneyUiError {
        return AddMoneyUiError(message = message)
    }
}
