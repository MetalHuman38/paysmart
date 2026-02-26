package net.metalbrain.paysmart.core.features.sendmoney.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fx.data.FxQuote
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteDataSource
import net.metalbrain.paysmart.core.features.sendmoney.domain.BankRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.DocumentRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.EmailRequestRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientFlowStep
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientMethod
import net.metalbrain.paysmart.core.features.sendmoney.domain.SendMoneyRecipientDraft
import net.metalbrain.paysmart.core.features.sendmoney.domain.VoltpayLookupRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.viewmodel.SendMoneyViewModel
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyRecipientScreen(
    onBack: () -> Unit,
    viewModel: SendMoneyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val draft = state.draft

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stepTitle(state.currentStep)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.currentStep == RecipientFlowStep.METHOD_PICKER) {
                                onBack()
                            } else {
                                viewModel.previousStep()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isHydrating) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecipientStepStatusCard(currentStep = state.currentStep)

            when (state.currentStep) {
                RecipientFlowStep.METHOD_PICKER -> MethodPickerStep(
                    draft = draft,
                    isQuoteLoading = state.isQuoteLoading,
                    quote = state.quote,
                    quoteDataSource = state.quoteDataSource,
                    quoteError = state.quoteError,
                    onMethodSelected = viewModel::selectMethod,
                    onSourceAmountChanged = viewModel::updateSourceAmountInput,
                    onSourceCurrencyChanged = viewModel::updateSourceCurrency,
                    onTargetCurrencyChanged = viewModel::updateTargetCurrency,
                    onRotateQuoteMethod = viewModel::rotateQuoteMethod,
                    onRefreshQuote = viewModel::refreshQuote
                )

                RecipientFlowStep.DETAILS -> RecipientDetailsStep(
                    draft = draft,
                    onVoltpayLookupChanged = viewModel::updateVoltpayLookup,
                    onBankDetailsChanged = viewModel::updateBankDetails,
                    onDocumentUploadChanged = viewModel::updateDocumentUpload,
                    onEmailRequestChanged = viewModel::updateEmailRequest
                )

                RecipientFlowStep.REVIEW -> RecipientReviewStep(
                    draft = draft,
                    quote = state.quote,
                    quoteDataSource = state.quoteDataSource
                )

                RecipientFlowStep.DONE -> RecipientDoneStep()
            }

            state.error?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (state.isPersisting) {
                Text(
                    text = "Saving recipient draft...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RecipientActionSection(
                currentStep = state.currentStep,
                canAdvance = state.canAdvance,
                onBack = viewModel::previousStep,
                onNext = viewModel::nextStep,
                onClear = viewModel::clearDraft,
                onDoneBack = onBack
            )
        }
    }
}

@Composable
private fun RecipientStepStatusCard(currentStep: RecipientFlowStep) {
    val labels = listOf(
        RecipientFlowStep.METHOD_PICKER to "Method",
        RecipientFlowStep.DETAILS to "Details",
        RecipientFlowStep.REVIEW to "Review",
        RecipientFlowStep.DONE to "Done"
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        FlowRow(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            labels.forEach { (step, label) ->
                val status = resolveStatus(step, currentStep)
                FilterChip(
                    selected = step == currentStep || status == "Completed",
                    onClick = { },
                    enabled = false,
                    label = { Text("$label: $status") }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MethodPickerStep(
    draft: SendMoneyRecipientDraft,
    isQuoteLoading: Boolean,
    quote: FxQuote?,
    quoteDataSource: FxQuoteDataSource?,
    quoteError: String?,
    onMethodSelected: (RecipientMethod) -> Unit,
    onSourceAmountChanged: (String) -> Unit,
    onSourceCurrencyChanged: (String) -> Unit,
    onTargetCurrencyChanged: (String) -> Unit,
    onRotateQuoteMethod: () -> Unit,
    onRefreshQuote: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "You send",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = draft.sourceCurrency,
                    onValueChange = onSourceCurrencyChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("From") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.sourceAmountInput,
                    onValueChange = onSourceAmountChanged,
                    modifier = Modifier.weight(2f),
                    label = { Text("Amount") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                )
            }

            Text(
                text = "Recipient gets",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft.targetCurrency,
                    onValueChange = onTargetCurrencyChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("To") },
                    singleLine = true
                )
                Text(
                    text = quote?.recipientAmount?.toString() ?: "0.00",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            quote?.let { currentQuote ->
                Text(
                    text = "1 ${currentQuote.sourceCurrency} = ${currentQuote.rate} ${currentQuote.targetCurrency}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Quote source: ${quoteDataSource?.name ?: currentQuote.rateSource}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                text = "Switch FX method: ${draft.quoteMethod.label}",
                onClick = onRotateQuoteMethod
            )

            PrimaryButton(
                text = "Refresh live FX quote",
                onClick = onRefreshQuote,
                isLoading = isQuoteLoading
            )

            quoteError?.takeIf { it.isNotBlank() }?.let { quoteFailure ->
                Text(
                    text = quoteFailure,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Add a recipient",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            RecipientMethod.entries.forEach { method ->
                val isSelected = draft.selectedMethod == method
                RecipientMethodRow(
                    method = method,
                    selected = isSelected,
                    onClick = { onMethodSelected(method) }
                )
            }
        }
    }
}

@Composable
private fun RecipientMethodRow(
    method: RecipientMethod,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = method.icon(),
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.label(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = method.description(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecipientDetailsStep(
    draft: SendMoneyRecipientDraft,
    onVoltpayLookupChanged: (VoltpayLookupRecipientForm) -> Unit,
    onBankDetailsChanged: (BankRecipientForm) -> Unit,
    onDocumentUploadChanged: (DocumentRecipientForm) -> Unit,
    onEmailRequestChanged: (EmailRequestRecipientForm) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Selected method: ${draft.selectedMethod.label()}",
                style = MaterialTheme.typography.titleSmall
            )

            when (draft.selectedMethod) {
                RecipientMethod.VOLTPAY_LOOKUP -> {
                    val form = draft.voltpayLookup
                    OutlinedTextField(
                        value = form.voltTag,
                        onValueChange = { onVoltpayLookupChanged(form.copy(voltTag = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Volt tag") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.email,
                        onValueChange = { onVoltpayLookupChanged(form.copy(email = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Recipient email") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.mobile,
                        onValueChange = { onVoltpayLookupChanged(form.copy(mobile = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Recipient mobile") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.note,
                        onValueChange = { onVoltpayLookupChanged(form.copy(note = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Note") }
                    )
                }

                RecipientMethod.BANK_DETAILS -> {
                    val form = draft.bankDetails
                    OutlinedTextField(
                        value = form.fullName,
                        onValueChange = { onBankDetailsChanged(form.copy(fullName = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Full name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.iban,
                        onValueChange = { onBankDetailsChanged(form.copy(iban = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("IBAN") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.bic,
                        onValueChange = { onBankDetailsChanged(form.copy(bic = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("BIC (optional)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.swift,
                        onValueChange = { onBankDetailsChanged(form.copy(swift = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("SWIFT (optional)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.bankName,
                        onValueChange = { onBankDetailsChanged(form.copy(bankName = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Bank name") },
                        singleLine = true
                    )
                }

                RecipientMethod.DOCUMENT_UPLOAD -> {
                    val form = draft.documentUpload
                    OutlinedTextField(
                        value = form.fileRef,
                        onValueChange = { onDocumentUploadChanged(form.copy(fileRef = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Document file reference") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.docType,
                        onValueChange = { onDocumentUploadChanged(form.copy(docType = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Document type") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.note,
                        onValueChange = { onDocumentUploadChanged(form.copy(note = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Note") }
                    )
                }

                RecipientMethod.EMAIL_REQUEST -> {
                    val form = draft.emailRequest
                    OutlinedTextField(
                        value = form.email,
                        onValueChange = { onEmailRequestChanged(form.copy(email = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Recipient email") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.fullName,
                        onValueChange = { onEmailRequestChanged(form.copy(fullName = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Recipient full name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.note,
                        onValueChange = { onEmailRequestChanged(form.copy(note = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Note") }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipientReviewStep(
    draft: SendMoneyRecipientDraft,
    quote: FxQuote?,
    quoteDataSource: FxQuoteDataSource?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Review recipient",
                style = MaterialTheme.typography.titleSmall
            )
            Text("Method: ${draft.selectedMethod.label()}")
            Text("Amount: ${draft.sourceAmountInput.ifBlank { "0" }} ${draft.sourceCurrency}")
            Text("Recipient currency: ${draft.targetCurrency}")
            quote?.let { currentQuote ->
                Text(
                    text = "FX: 1 ${currentQuote.sourceCurrency} = ${currentQuote.rate} ${currentQuote.targetCurrency}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Quoted recipient amount: ${currentQuote.recipientAmount} ${currentQuote.targetCurrency}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Quote source: ${quoteDataSource?.name ?: currentQuote.rateSource}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = when (draft.selectedMethod) {
                    RecipientMethod.VOLTPAY_LOOKUP -> {
                        val lookup = draft.voltpayLookup
                        "Lookup: tag=${lookup.voltTag.ifBlank { "-" }}, email=${lookup.email.ifBlank { "-" }}, mobile=${lookup.mobile.ifBlank { "-" }}"
                    }

                    RecipientMethod.BANK_DETAILS -> {
                        val bank = draft.bankDetails
                        "Bank: ${bank.fullName.ifBlank { "-" }}, IBAN=${bank.iban.ifBlank { "-" }}"
                    }

                    RecipientMethod.DOCUMENT_UPLOAD -> {
                        val document = draft.documentUpload
                        "Document: ref=${document.fileRef.ifBlank { "-" }}, type=${document.docType.ifBlank { "-" }}"
                    }

                    RecipientMethod.EMAIL_REQUEST -> {
                        val email = draft.emailRequest
                        "Email request: ${email.email.ifBlank { "-" }}, name=${email.fullName.ifBlank { "-" }}"
                    }
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RecipientDoneStep() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Recipient draft confirmed",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "This completes SEND-002 UI flow state. Transfer creation comes in SEND-003/004.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecipientActionSection(
    currentStep: RecipientFlowStep,
    canAdvance: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onClear: () -> Unit,
    onDoneBack: () -> Unit
) {
    when (currentStep) {
        RecipientFlowStep.METHOD_PICKER -> {
            PrimaryButton(
                text = "Continue to details",
                onClick = onNext,
                enabled = canAdvance
            )
        }

        RecipientFlowStep.DETAILS -> {
            OutlinedButton(
                text = "Back to method picker",
                onClick = onBack
            )
            PrimaryButton(
                text = "Review recipient",
                onClick = onNext,
                enabled = canAdvance
            )
        }

        RecipientFlowStep.REVIEW -> {
            OutlinedButton(
                text = "Edit recipient",
                onClick = onBack
            )
            PrimaryButton(
                text = "Confirm recipient",
                onClick = onNext,
                enabled = canAdvance
            )
        }

        RecipientFlowStep.DONE -> {
            OutlinedButton(
                text = "Back to home",
                onClick = onDoneBack
            )
            PrimaryButton(
                text = "Start another recipient",
                onClick = onClear
            )
        }
    }
}

private fun stepTitle(step: RecipientFlowStep): String {
    return when (step) {
        RecipientFlowStep.METHOD_PICKER -> "Add a recipient"
        RecipientFlowStep.DETAILS -> "Recipient details"
        RecipientFlowStep.REVIEW -> "Review recipient"
        RecipientFlowStep.DONE -> "Recipient ready"
    }
}

private fun resolveStatus(step: RecipientFlowStep, currentStep: RecipientFlowStep): String {
    val stepIndex = RecipientFlowStep.entries.indexOf(step)
    val currentIndex = RecipientFlowStep.entries.indexOf(currentStep)
    return when {
        currentStep == RecipientFlowStep.DONE && step == RecipientFlowStep.DONE -> "Completed"
        stepIndex < currentIndex -> "Completed"
        stepIndex == currentIndex -> "In progress"
        else -> "Pending"
    }
}

private fun RecipientMethod.label(): String {
    return when (this) {
        RecipientMethod.VOLTPAY_LOOKUP -> "Find on PaySmart"
        RecipientMethod.BANK_DETAILS -> "Bank details"
        RecipientMethod.DOCUMENT_UPLOAD -> "Upload screenshot or invoice"
        RecipientMethod.EMAIL_REQUEST -> "Pay by email"
    }
}

private fun RecipientMethod.description(): String {
    return when (this) {
        RecipientMethod.VOLTPAY_LOOKUP -> "Search by tag, email, or mobile number"
        RecipientMethod.BANK_DETAILS -> "Enter recipient name and IBAN"
        RecipientMethod.DOCUMENT_UPLOAD -> "We fill recipient details from screenshot/photo/PDF"
        RecipientMethod.EMAIL_REQUEST -> "Request recipient bank details by email"
    }
}

private fun RecipientMethod.icon(): ImageVector {
    return when (this) {
        RecipientMethod.VOLTPAY_LOOKUP -> Icons.Default.PersonSearch
        RecipientMethod.BANK_DETAILS -> Icons.Default.AccountBalance
        RecipientMethod.DOCUMENT_UPLOAD -> Icons.Default.Description
        RecipientMethod.EMAIL_REQUEST -> Icons.Default.Email
    }
}
