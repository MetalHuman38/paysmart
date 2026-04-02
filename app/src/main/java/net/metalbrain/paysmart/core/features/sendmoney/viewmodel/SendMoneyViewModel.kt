package net.metalbrain.paysmart.core.features.sendmoney.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.core.features.fx.data.FxQuote
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteDataSource
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteQuery
import net.metalbrain.paysmart.core.features.fx.repository.FxQuoteRepository
import net.metalbrain.paysmart.core.features.sendmoney.data.RecentSendRecipientRepository
import net.metalbrain.paysmart.core.features.sendmoney.data.SendMoneyRecipientDraftRepository
import net.metalbrain.paysmart.core.features.sendmoney.domain.BankRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.DocumentRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.EmailRequestRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientFlowStep
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientMethod
import net.metalbrain.paysmart.core.features.sendmoney.domain.SendMoneyRecipientDraft
import net.metalbrain.paysmart.core.features.sendmoney.domain.VoltpayLookupRecipientForm
import net.metalbrain.paysmart.navigator.Screen
import java.util.Locale

data class SendMoneyRecipientUiState(
    val userId: String? = null,
    val draft: SendMoneyRecipientDraft = SendMoneyRecipientDraft(),
    val isHydrating: Boolean = true,
    val isPersisting: Boolean = false,
    val isQuoteLoading: Boolean = false,
    val quoteError: String? = null,
    val error: String? = null
) {
    val currentStep: RecipientFlowStep
        get() = draft.step

    val quote: FxQuote?
        get() = draft.quoteSnapshot

    val quoteDataSource: FxQuoteDataSource?
        get() = draft.quoteDataSource

    val canAdvance: Boolean
        get() = when (draft.step) {
            RecipientFlowStep.METHOD_PICKER -> true
            RecipientFlowStep.DETAILS -> draft.isSelectedMethodValid()
            RecipientFlowStep.REVIEW -> true
            RecipientFlowStep.DONE -> false
        }
}

@HiltViewModel
class SendMoneyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val draftRepository: SendMoneyRecipientDraftRepository,
    private val fxQuoteRepository: FxQuoteRepository,
    private val recentSendRecipientRepository: RecentSendRecipientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendMoneyRecipientUiState())
    val uiState: StateFlow<SendMoneyRecipientUiState> = _uiState.asStateFlow()
    private var quoteRefreshJob: Job? = null
    private var latestQuoteRequestId: Long = 0L
    private val prefillRecipientKey = savedStateHandle
        .get<String>(Screen.SendMoney.RECIPIENT_KEY_ARG)
        .orEmpty()
        .trim()

    init {
        observeRecipientDraft()
        if (prefillRecipientKey.isNotEmpty()) {
            prefillRecentRecipient(prefillRecipientKey)
        }
    }

    fun selectMethod(method: RecipientMethod) {
        mutateDraft { draft ->
            draft.copy(
                selectedMethod = method,
                step = if (draft.step == RecipientFlowStep.METHOD_PICKER) {
                    RecipientFlowStep.METHOD_PICKER
                } else {
                    RecipientFlowStep.DETAILS
                }
            )
        }
    }

    fun updateSourceAmountInput(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' }.take(12)
        val firstDotIndex = filtered.indexOf('.')
        val sanitized = if (firstDotIndex >= 0) {
            val beforeDot = filtered.substring(0, firstDotIndex + 1)
            val afterDot = filtered.substring(firstDotIndex + 1).replace(".", "")
            beforeDot + afterDot
        } else {
            filtered
        }
        mutateDraft { draft ->
            draft.copy(
                sourceAmountInput = sanitized,
                quoteSnapshot = null,
                quoteDataSource = null
            )
        }
        _uiState.update { it.copy(quoteError = null) }
        scheduleQuoteRefresh()
    }

    fun updateSourceCurrency(input: String) {
        val normalized = input.filter { it.isLetter() }.take(3).uppercase(Locale.US)
        mutateDraft { draft ->
            draft.copy(
                sourceCurrency = normalized.ifBlank { draft.sourceCurrency },
                quoteSnapshot = null,
                quoteDataSource = null
            )
        }
        _uiState.update { it.copy(quoteError = null) }
        scheduleQuoteRefresh()
    }

    fun updateTargetCurrency(input: String) {
        val normalized = input.filter { it.isLetter() }.take(3).uppercase(Locale.US)
        mutateDraft { draft ->
            draft.copy(
                targetCurrency = normalized.ifBlank { draft.targetCurrency },
                quoteSnapshot = null,
                quoteDataSource = null
            )
        }
        _uiState.update { it.copy(quoteError = null) }
        scheduleQuoteRefresh()
    }

    fun rotateQuoteMethod() {
        mutateDraft { draft ->
            draft.copy(
                quoteMethod = draft.quoteMethod.next(),
                quoteSnapshot = null,
                quoteDataSource = null
            )
        }
        _uiState.update { it.copy(quoteError = null) }
        scheduleQuoteRefresh()
    }

    fun refreshQuote() {
        val query = buildQuoteQueryOrNull()
        if (query == null) {
            latestQuoteRequestId++
            _uiState.update {
                it.copy(
                    quoteError = "Enter a valid amount and currencies to fetch live quote",
                    isQuoteLoading = false
                )
            }
            return
        }

        val requestId = ++latestQuoteRequestId

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isQuoteLoading = true,
                    quoteError = null,
                    error = null
                )
            }

            fxQuoteRepository.getQuote(query)
                .onSuccess { result ->
                    if (requestId != latestQuoteRequestId) {
                        return@onSuccess
                    }
                    mutateDraft { draft ->
                        draft.copy(
                            quoteMethod = query.method,
                            quoteSnapshot = result.quote,
                            quoteDataSource = result.dataSource
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isQuoteLoading = false,
                            quoteError = null
                        )
                    }
                }
                .onFailure { error ->
                    if (requestId != latestQuoteRequestId) {
                        return@onFailure
                    }
                    _uiState.update {
                        it.copy(
                            isQuoteLoading = false,
                            quoteError = error.localizedMessage ?: "Unable to fetch quote"
                        )
                    }
                }
        }
    }

    fun updateVoltpayLookup(form: VoltpayLookupRecipientForm) {
        mutateDraft { draft -> draft.copy(voltpayLookup = form) }
    }

    fun updateBankDetails(form: BankRecipientForm) {
        mutateDraft { draft -> draft.copy(bankDetails = form) }
    }

    fun updateDocumentUpload(form: DocumentRecipientForm) {
        mutateDraft { draft -> draft.copy(documentUpload = form) }
    }

    fun updateEmailRequest(form: EmailRequestRecipientForm) {
        mutateDraft { draft -> draft.copy(emailRequest = form) }
    }

    fun nextStep() {
        when (_uiState.value.draft.step) {
            RecipientFlowStep.METHOD_PICKER -> {
                mutateDraft { draft -> draft.copy(step = RecipientFlowStep.DETAILS) }
            }

            RecipientFlowStep.DETAILS -> {
                if (!_uiState.value.draft.isSelectedMethodValid()) {
                    _uiState.update {
                        it.copy(error = "Complete recipient details for the selected method")
                    }
                    return
                }
                mutateDraft { draft -> draft.copy(step = RecipientFlowStep.REVIEW) }
            }

            RecipientFlowStep.REVIEW -> {
                confirmRecipient()
            }

            RecipientFlowStep.DONE -> Unit
        }
    }

    fun previousStep() {
        when (_uiState.value.draft.step) {
            RecipientFlowStep.METHOD_PICKER -> Unit
            RecipientFlowStep.DETAILS -> mutateDraft { draft ->
                draft.copy(step = RecipientFlowStep.METHOD_PICKER)
            }

            RecipientFlowStep.REVIEW -> mutateDraft { draft ->
                draft.copy(step = RecipientFlowStep.DETAILS)
            }

            RecipientFlowStep.DONE -> mutateDraft { draft ->
                draft.copy(step = RecipientFlowStep.REVIEW)
            }
        }
    }

    fun resetFlow() {
        mutateDraft { draft -> draft.copy(step = RecipientFlowStep.METHOD_PICKER) }
    }

    fun clearDraft() {
        val userId = _uiState.value.userId
        if (userId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isHydrating = false,
                    isPersisting = false,
                    error = "Authentication required before starting send money"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPersisting = true, error = null) }
            runCatching { draftRepository.clear(userId) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            draft = SendMoneyRecipientDraft(),
                            isPersisting = false,
                            quoteError = null,
                            isQuoteLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            error = error.localizedMessage ?: "Unable to clear recipient draft"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun observeRecipientDraft() {
        val userId = authRepository.currentUser?.uid?.trim()
        if (userId.isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    isHydrating = false,
                    error = "Authentication required before starting send money"
                )
            }
            return
        }

        _uiState.update { it.copy(userId = userId, isHydrating = true, error = null) }

        viewModelScope.launch {
            draftRepository.observeByUserId(userId).collectLatest { savedDraft ->
                _uiState.update { current ->
                    current.copy(
                        draft = savedDraft ?: SendMoneyRecipientDraft(),
                        isHydrating = false,
                        isPersisting = false,
                        isQuoteLoading = false
                    )
                }
            }
        }
    }

    private fun mutateDraft(transform: (SendMoneyRecipientDraft) -> SendMoneyRecipientDraft) {
        val userId = _uiState.value.userId
        if (userId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isHydrating = false,
                    isPersisting = false,
                    error = "Authentication required before starting send money"
                )
            }
            return
        }

        val updatedDraft = transform(_uiState.value.draft)
            .normalized()
            .withUpdatedTimestamp()
        _uiState.update {
            it.copy(
                draft = updatedDraft,
                isPersisting = true,
                error = null
            )
        }

        viewModelScope.launch {
            runCatching { draftRepository.upsert(userId, updatedDraft) }
                .onSuccess {
                    _uiState.update {
                        it.copy(isPersisting = false)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            error = error.localizedMessage ?: "Unable to persist recipient draft"
                        )
                    }
                }
        }
    }

    private fun confirmRecipient() {
        val userId = _uiState.value.userId
        if (userId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isHydrating = false,
                    isPersisting = false,
                    error = "Authentication required before starting send money"
                )
            }
            return
        }

        val completedDraft = _uiState.value.draft
            .copy(step = RecipientFlowStep.DONE)
            .normalized()
            .withUpdatedTimestamp()

        _uiState.update {
            it.copy(
                draft = completedDraft,
                isPersisting = true,
                error = null
            )
        }

        viewModelScope.launch {
            runCatching {
                recentSendRecipientRepository.recordFromDraft(userId, completedDraft)
                draftRepository.upsert(userId, completedDraft)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        draft = completedDraft,
                        isPersisting = false,
                        error = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isPersisting = false,
                        error = error.localizedMessage ?: "Unable to save recipient"
                    )
                }
            }
        }
    }

    private fun prefillRecentRecipient(recipientKey: String) {
        val userId = authRepository.currentUser?.uid?.trim()
        if (userId.isNullOrEmpty()) {
            return
        }

        viewModelScope.launch {
            val recipient = recentSendRecipientRepository.getByKey(
                userId = userId,
                recipientKey = recipientKey
            ) ?: return@launch

            val prefilledDraft = recipient.toPrefilledDraft()
                .normalized()
                .withUpdatedTimestamp()

            _uiState.update {
                it.copy(
                    userId = userId,
                    draft = prefilledDraft,
                    isHydrating = false,
                    quoteError = null,
                    error = null
                )
            }

            runCatching {
                draftRepository.upsert(userId, prefilledDraft)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(error = error.localizedMessage ?: "Unable to load recipient")
                }
            }
        }
    }

    private fun scheduleQuoteRefresh() {
        quoteRefreshJob?.cancel()
        quoteRefreshJob = viewModelScope.launch {
            delay(250)
            refreshQuote()
        }
    }

    private fun buildQuoteQueryOrNull(): FxQuoteQuery? {
        val sourceAmount = parseAmountToMajor(_uiState.value.draft.sourceAmountInput) ?: return null
        val sourceCurrency = _uiState.value.draft.sourceCurrency.trim().uppercase(Locale.US)
        val targetCurrency = _uiState.value.draft.targetCurrency.trim().uppercase(Locale.US)
        if (sourceCurrency.length != 3 || targetCurrency.length != 3) {
            return null
        }

        return FxQuoteQuery(
            sourceCurrency = sourceCurrency,
            targetCurrency = targetCurrency,
            sourceAmount = sourceAmount,
            method = _uiState.value.draft.quoteMethod
        )
    }

    private fun parseAmountToMajor(raw: String): Double? {
        val normalized = raw.trim()
        if (normalized.isBlank()) return null
        return normalized.toDoubleOrNull()?.takeIf { it > 0.0 }
    }
}
