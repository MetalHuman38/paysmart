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
                    text = stringResource(R.string.send_money_saving_draft),
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
        RecipientFlowStep.METHOD_PICKER to stringResource(R.string.send_money_step_method),
        RecipientFlowStep.DETAILS to stringResource(R.string.send_money_step_details),
        RecipientFlowStep.REVIEW to stringResource(R.string.send_money_step_review),
        RecipientFlowStep.DONE to stringResource(R.string.send_money_step_done)
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
                    selected = step == currentStep || status == stringResource(R.string.send_money_status_completed),
                    onClick = { },
                    enabled = false,
                    label = {
                        Text(
                            text = stringResource(
                                R.string.send_money_step_chip_format,
                                label,
                                status
                            )
                        )
                    }
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
                text = stringResource(R.string.send_money_you_send),
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
                    label = { Text(stringResource(R.string.send_money_from_label)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.sourceAmountInput,
                    onValueChange = onSourceAmountChanged,
                    modifier = Modifier.weight(2f),
                    label = { Text(stringResource(R.string.send_money_amount_label)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                )
            }

            Text(
                text = stringResource(R.string.send_money_recipient_gets),
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
                    label = { Text(stringResource(R.string.send_money_to_label)) },
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
                    text = stringResource(
                        R.string.send_money_rate_format,
                        currentQuote.sourceCurrency,
                        currentQuote.rate.toString(),
                        currentQuote.targetCurrency
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(
                        R.string.send_money_quote_source_format,
                        quoteDataSource?.name ?: currentQuote.rateSource
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                text = stringResource(
                    R.string.send_money_switch_method_action,
                    draft.quoteMethod.label
                ),
                onClick = onRotateQuoteMethod
            )

            PrimaryButton(
                text = stringResource(R.string.send_money_refresh_quote_action),
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
                text = stringResource(R.string.send_money_title_add_recipient),
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
                text = stringResource(
                    R.string.send_money_selected_method_format,
                    draft.selectedMethod.label()
                ),
                style = MaterialTheme.typography.titleSmall
            )

            when (draft.selectedMethod) {
                RecipientMethod.VOLTPAY_LOOKUP -> {
                    val form = draft.voltpayLookup
                    OutlinedTextField(
                        value = form.voltTag,
                        onValueChange = { onVoltpayLookupChanged(form.copy(voltTag = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_volt_tag)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.email,
                        onValueChange = { onVoltpayLookupChanged(form.copy(email = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_recipient_email)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.mobile,
                        onValueChange = { onVoltpayLookupChanged(form.copy(mobile = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_recipient_mobile)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.note,
                        onValueChange = { onVoltpayLookupChanged(form.copy(note = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_note)) }
                    )
                }

                RecipientMethod.BANK_DETAILS -> {
                    val form = draft.bankDetails
                    OutlinedTextField(
                        value = form.fullName,
                        onValueChange = { onBankDetailsChanged(form.copy(fullName = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_full_name)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.iban,
                        onValueChange = { onBankDetailsChanged(form.copy(iban = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_iban)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.bic,
                        onValueChange = { onBankDetailsChanged(form.copy(bic = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_bic_optional)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.swift,
                        onValueChange = { onBankDetailsChanged(form.copy(swift = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_swift_optional)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.bankName,
                        onValueChange = { onBankDetailsChanged(form.copy(bankName = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_bank_name)) },
                        singleLine = true
                    )
                }

                RecipientMethod.DOCUMENT_UPLOAD -> {
                    val form = draft.documentUpload
                    OutlinedTextField(
                        value = form.fileRef,
                        onValueChange = { onDocumentUploadChanged(form.copy(fileRef = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_document_reference)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.docType,
                        onValueChange = { onDocumentUploadChanged(form.copy(docType = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_document_type)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.note,
                        onValueChange = { onDocumentUploadChanged(form.copy(note = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_note)) }
                    )
                }

                RecipientMethod.EMAIL_REQUEST -> {
                    val form = draft.emailRequest
                    OutlinedTextField(
                        value = form.email,
                        onValueChange = { onEmailRequestChanged(form.copy(email = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_recipient_email)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.fullName,
                        onValueChange = { onEmailRequestChanged(form.copy(fullName = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_recipient_full_name)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.note,
                        onValueChange = { onEmailRequestChanged(form.copy(note = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.send_money_field_note)) }
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
                text = stringResource(R.string.send_money_review_title),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                stringResource(
                    R.string.send_money_review_method_format,
                    draft.selectedMethod.label()
                )
            )
            Text(
                stringResource(
                    R.string.send_money_review_amount_format,
                    draft.sourceAmountInput.ifBlank { "0" },
                    draft.sourceCurrency
                )
            )
            Text(
                stringResource(
                    R.string.send_money_review_recipient_currency_format,
                    draft.targetCurrency
                )
            )
            quote?.let { currentQuote ->
                Text(
                    text = stringResource(
                        R.string.send_money_review_fx_format,
                        currentQuote.sourceCurrency,
                        currentQuote.rate.toString(),
                        currentQuote.targetCurrency
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(
                        R.string.send_money_review_recipient_amount_format,
                        currentQuote.recipientAmount.toString(),
                        currentQuote.targetCurrency
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(
                        R.string.send_money_quote_source_format,
                        quoteDataSource?.name ?: currentQuote.rateSource
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = when (draft.selectedMethod) {
                    RecipientMethod.VOLTPAY_LOOKUP -> {
                        val lookup = draft.voltpayLookup
                        stringResource(
                            R.string.send_money_review_lookup_format,
                            lookup.voltTag.ifBlank { stringResource(R.string.send_money_dash) },
                            lookup.email.ifBlank { stringResource(R.string.send_money_dash) },
                            lookup.mobile.ifBlank { stringResource(R.string.send_money_dash) }
                        )
                    }

                    RecipientMethod.BANK_DETAILS -> {
                        val bank = draft.bankDetails
                        stringResource(
                            R.string.send_money_review_bank_format,
                            bank.fullName.ifBlank { stringResource(R.string.send_money_dash) },
                            bank.iban.ifBlank { stringResource(R.string.send_money_dash) }
                        )
                    }

                    RecipientMethod.DOCUMENT_UPLOAD -> {
                        val document = draft.documentUpload
                        stringResource(
                            R.string.send_money_review_document_format,
                            document.fileRef.ifBlank { stringResource(R.string.send_money_dash) },
                            document.docType.ifBlank { stringResource(R.string.send_money_dash) }
                        )
                    }

                    RecipientMethod.EMAIL_REQUEST -> {
                        val email = draft.emailRequest
                        stringResource(
                            R.string.send_money_review_email_request_format,
                            email.email.ifBlank { stringResource(R.string.send_money_dash) },
                            email.fullName.ifBlank { stringResource(R.string.send_money_dash) }
                        )
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
                text = stringResource(R.string.send_money_done_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.send_money_done_description),
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
                text = stringResource(R.string.send_money_action_continue_details),
                onClick = onNext,
                enabled = canAdvance
            )
        }

        RecipientFlowStep.DETAILS -> {
            OutlinedButton(
                text = stringResource(R.string.send_money_action_back_method),
                onClick = onBack
            )
            PrimaryButton(
                text = stringResource(R.string.send_money_action_review),
                onClick = onNext,
                enabled = canAdvance
            )
        }

        RecipientFlowStep.REVIEW -> {
            OutlinedButton(
                text = stringResource(R.string.send_money_action_edit),
                onClick = onBack
            )
            PrimaryButton(
                text = stringResource(R.string.send_money_action_confirm),
                onClick = onNext,
                enabled = canAdvance
            )
        }

        RecipientFlowStep.DONE -> {
            OutlinedButton(
                text = stringResource(R.string.send_money_action_back_home),
                onClick = onDoneBack
            )
            PrimaryButton(
                text = stringResource(R.string.send_money_action_start_another),
                onClick = onClear
            )
        }
    }
}

@Composable
private fun stepTitle(step: RecipientFlowStep): String {
    return when (step) {
        RecipientFlowStep.METHOD_PICKER -> stringResource(R.string.send_money_title_add_recipient)
        RecipientFlowStep.DETAILS -> stringResource(R.string.send_money_title_recipient_details)
        RecipientFlowStep.REVIEW -> stringResource(R.string.send_money_title_review_recipient)
        RecipientFlowStep.DONE -> stringResource(R.string.send_money_title_recipient_ready)
    }
}

@Composable
private fun resolveStatus(step: RecipientFlowStep, currentStep: RecipientFlowStep): String {
    val stepIndex = RecipientFlowStep.entries.indexOf(step)
    val currentIndex = RecipientFlowStep.entries.indexOf(currentStep)
    return when {
        currentStep == RecipientFlowStep.DONE && step == RecipientFlowStep.DONE ->
            stringResource(R.string.send_money_status_completed)
        stepIndex < currentIndex -> stringResource(R.string.send_money_status_completed)
        stepIndex == currentIndex -> stringResource(R.string.send_money_status_in_progress)
        else -> stringResource(R.string.send_money_status_pending)
    }
}

@Composable
private fun RecipientMethod.label(): String {
    return when (this) {
        RecipientMethod.VOLTPAY_LOOKUP -> stringResource(R.string.send_money_method_find_paysmart)
        RecipientMethod.BANK_DETAILS -> stringResource(R.string.send_money_method_bank_details)
        RecipientMethod.DOCUMENT_UPLOAD -> stringResource(R.string.send_money_method_upload_invoice)
        RecipientMethod.EMAIL_REQUEST -> stringResource(R.string.send_money_method_pay_email)
    }
}

@Composable
private fun RecipientMethod.description(): String {
    return when (this) {
        RecipientMethod.VOLTPAY_LOOKUP -> stringResource(R.string.send_money_method_desc_find_paysmart)
        RecipientMethod.BANK_DETAILS -> stringResource(R.string.send_money_method_desc_bank_details)
        RecipientMethod.DOCUMENT_UPLOAD -> stringResource(R.string.send_money_method_desc_upload_invoice)
        RecipientMethod.EMAIL_REQUEST -> stringResource(R.string.send_money_method_desc_pay_email)
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
