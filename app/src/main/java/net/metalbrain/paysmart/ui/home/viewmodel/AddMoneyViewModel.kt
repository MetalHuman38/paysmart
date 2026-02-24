package net.metalbrain.paysmart.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.WalletBalanceRepository
import net.metalbrain.paysmart.ui.home.addmoney.AddMoneyPaymentSheetLaunch
import net.metalbrain.paysmart.ui.home.addmoney.AddMoneyRepository
import net.metalbrain.paysmart.ui.home.addmoney.AddMoneySessionStatus
import net.metalbrain.paysmart.ui.home.addmoney.AddMoneyUiState
import net.metalbrain.paysmart.ui.home.fx.FxQuoteQuery
import net.metalbrain.paysmart.ui.home.fx.FxQuoteRepository
import net.metalbrain.paysmart.ui.home.fx.FxPaymentMethod

@HiltViewModel
class AddMoneyViewModel @Inject constructor(
    private val addMoneyRepository: AddMoneyRepository,
    private val authRepository: AuthRepository,
    private val walletBalanceRepository: WalletBalanceRepository,
    private val fxQuoteRepository: FxQuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMoneyUiState())
    val uiState: StateFlow<AddMoneyUiState> = _uiState.asStateFlow()
    private var quoteRefreshJob: Job? = null

    init {
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
        _uiState.update {
            it.copy(
                quoteMethod = method,
                quoteError = null
            )
        }
        scheduleQuoteRefresh()
    }

    fun rotateQuoteMethod() {
        val next = _uiState.value.quoteMethod.next()
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
                it.copy(error = "Enter a valid amount")
            }
            return
        }

        val currency = _uiState.value.currency.ifBlank { "GBP" }.uppercase()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    error = null,
                    infoMessage = null
                )
            }

            addMoneyRepository.createSession(amountMinor, currency)
                .onSuccess { session ->
                    val paymentIntentClientSecret = session.paymentIntentClientSecret
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                    val publishableKey = session.publishableKey
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }

                    if (paymentIntentClientSecret == null || publishableKey == null) {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                sessionId = session.sessionId,
                                sessionStatus = session.status,
                                error = "Payment session is missing PaymentSheet configuration"
                            )
                        }
                        return@onSuccess
                    }

                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            sessionId = session.sessionId,
                            sessionStatus = session.status,
                            paymentSheetLaunch = AddMoneyPaymentSheetLaunch(
                                publishableKey = publishableKey,
                                paymentIntentClientSecret = paymentIntentClientSecret
                            ),
                            infoMessage = when (session.status) {
                                AddMoneySessionStatus.SUCCEEDED -> "Payment completed. Refresh to sync wallet."
                                AddMoneySessionStatus.EXPIRED -> "Session expired. Create a new top up session."
                                else -> "Payment sheet ready."
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = error.localizedMessage ?: "Unable to start add money flow"
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
    }

    fun onPaymentSheetCanceled() {
        _uiState.update {
            it.copy(
                infoMessage = "Payment canceled.",
                error = null
            )
        }
    }

    fun onPaymentSheetFailed(message: String?) {
        _uiState.update {
            it.copy(
                error = message?.takeIf { value -> value.isNotBlank() }
                    ?: "Payment failed before confirmation",
                infoMessage = null
            )
        }
    }

    fun refreshSessionStatus() {
        val sessionId = _uiState.value.sessionId
        if (sessionId.isNullOrBlank() || _uiState.value.isCheckingStatus) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCheckingStatus = true,
                    error = null,
                    infoMessage = null
                )
            }

            addMoneyRepository.getSessionStatus(sessionId)
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            isCheckingStatus = false,
                            sessionStatus = session.status,
                            infoMessage = when (session.status) {
                                AddMoneySessionStatus.SUCCEEDED -> "Top up completed. Wallet is syncing."
                                AddMoneySessionStatus.PENDING,
                                AddMoneySessionStatus.CREATED -> "Payment still pending."
                                AddMoneySessionStatus.EXPIRED -> "Session expired. Start again."
                                AddMoneySessionStatus.FAILED -> "Payment failed. Try again."
                            }
                        )
                    }

                    if (session.status == AddMoneySessionStatus.SUCCEEDED) {
                        syncWalletSnapshot()
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCheckingStatus = false,
                            error = error.localizedMessage ?: "Unable to refresh payment status"
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

    private fun parseAmountToMinor(raw: String): Int? {
        val normalized = raw.trim()
        if (normalized.isEmpty()) return null

        val amount = normalized.toBigDecimalOrNull() ?: return null
        val minor = amount.multiply(java.math.BigDecimal(100))
            .setScale(0, java.math.RoundingMode.HALF_UP)
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

    private fun buildQuoteQueryOrNull(): FxQuoteQuery? {
        val sourceAmount = parseAmountToMajor(_uiState.value.amountInput) ?: return null
        val sourceCurrency = _uiState.value.currency.trim().uppercase()
        val targetCurrency = _uiState.value.quoteTargetCurrency.trim().uppercase()
        if (sourceCurrency.length != 3 || targetCurrency.length != 3) return null

        return FxQuoteQuery(
            sourceCurrency = sourceCurrency,
            targetCurrency = targetCurrency,
            sourceAmount = sourceAmount,
            method = _uiState.value.quoteMethod
        )
    }
}
