package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.card.InvoiceDynamicSectionCard
import net.metalbrain.paysmart.core.features.invoicing.card.InvoiceLineItemsCard
import net.metalbrain.paysmart.core.features.invoicing.card.InvoiceProfessionSelectionCard
import net.metalbrain.paysmart.core.features.invoicing.components.InvoiceSetupBottomBar
import net.metalbrain.paysmart.core.features.invoicing.components.InvoiceSetupProgressHeader
import net.metalbrain.paysmart.core.features.invoicing.utils.canAdvanceCurrentStep
import net.metalbrain.paysmart.core.features.invoicing.utils.filteredCopy
import net.metalbrain.paysmart.core.features.invoicing.utils.filteredForReview
import net.metalbrain.paysmart.core.features.invoicing.utils.progressiveStepIndex
import net.metalbrain.paysmart.core.features.invoicing.utils.primaryLineRateValue
import net.metalbrain.paysmart.core.features.invoicing.utils.sectionOrNull
import net.metalbrain.paysmart.core.features.invoicing.utils.stepBody
import net.metalbrain.paysmart.core.features.invoicing.utils.stepTitle
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.core.features.invoicing.card.InvoiceReadOnlySectionCard
import net.metalbrain.paysmart.core.features.invoicing.card.InvoiceTotalsOverviewCard
import net.metalbrain.paysmart.core.invoice.model.InvoiceFieldKeys
import net.metalbrain.paysmart.core.invoice.model.InvoiceFormStep
import net.metalbrain.paysmart.core.invoice.model.Profession
import net.metalbrain.paysmart.ui.theme.Dimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSetupFlowScreen(
    state: InvoiceSetupUiState,
    onExit: () -> Unit,
    onBackStep: () -> Unit,
    onSelectProfession: (Profession) -> Unit,
    onSelectTemplate: (String) -> Unit,
    onSectionFieldChanged: (
        sectionId: String,
        fieldKey: String,
        value: Any?) -> Unit,
    onVenueNameChanged: (String) -> Unit,
    onVenueAddressChanged: (String) -> Unit,
    onVenueCountryChanged: (String) -> Unit,
    onVenueRateChanged: (String) -> Unit,
    onSearchAddress: () -> Unit,
    onApplySuggestedAddress: () -> Unit,
    onAddVenue: () -> Unit,
    onSelectVenue: (String) -> Unit,
    onLineItemFieldChanged: (index: Int, fieldKey: String, value: Any?) -> Unit,
    onSaveDraft: () -> Unit,
    onContinue: () -> Unit,
    onFinalize: () -> Unit,
    onOpenInvoice: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val currentStepIndex = progressiveStepIndex(state.formStep)
    val canContinue = state.canAdvanceCurrentStep()

    LaunchedEffect(state.formStep) {
        listState.animateScrollToItem(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.formStep == InvoiceFormStep.QUICK_START) {
                            stringResource(R.string.invoice_setup_title)
                        } else {
                            state.selectedTemplate?.name ?: stringResource(R.string.invoice_setup_title)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.formStep == InvoiceFormStep.QUICK_START) {
                                onExit()
                            } else {
                                onBackStep()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    if (state.formStep != InvoiceFormStep.QUICK_START) {
                        TextButton(
                            onClick = onSaveDraft,
                            enabled = !state.isPersisting && !state.isFinalizing
                        ) {
                            Text(text = stringResource(R.string.invoice_setup_save_draft_action))
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!state.isHydrating) {
                InvoiceSetupBottomBar(
                    state = state,
                    canContinue = canContinue,
                    onContinue = onContinue,
                    onFinalize = onFinalize
                )
            }
        }
    ) { innerPadding ->
        if (state.isHydrating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Dimens.md,
                top = Dimens.md,
                end = Dimens.md,
                bottom = Dimens.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            item {
                InvoiceSetupProgressHeader(
                    state = state,
                    currentStepIndex = currentStepIndex,
                    title = stepTitle(state.formStep),
                    body = stepBody(state.formStep),
                    onSelectTemplate = onSelectTemplate
                )
            }

            when (state.formStep) {
                InvoiceFormStep.QUICK_START -> {
                    item {
                        InvoiceProfessionSelectionCard(
                            professions = state.availableProfessions,
                            selectedProfessionId = state.selectedProfession?.id,
                            onSelectProfession = onSelectProfession
                        )
                    }
                }

                InvoiceFormStep.INVOICE_INFO -> {
                    state.draftInvoice.sectionOrNull("invoice_info")?.let { section ->
                        item {
                            InvoiceDynamicSectionCard(
                                section = section,
                                onFieldChanged = { fieldKey, value ->
                                    onSectionFieldChanged(section.id, fieldKey, value)
                                }
                            )
                        }
                    }
                }

                InvoiceFormStep.WORKER_DETAILS -> {
                    state.draftInvoice.sectionOrNull("worker_details")?.let { section ->
                        item {
                            InvoiceDynamicSectionCard(
                                section = section,
                                onFieldChanged = { fieldKey, value ->
                                    onSectionFieldChanged(section.id, fieldKey, value)
                                }
                            )
                        }
                    }
                }

                InvoiceFormStep.CLIENT_DETAILS -> {
                    item {
                        InvoiceVenueSetupSection(
                            state = state,
                            onVenueNameChanged = onVenueNameChanged,
                            onVenueAddressChanged = onVenueAddressChanged,
                            onVenueCountryChanged = onVenueCountryChanged,
                            onVenueRateChanged = onVenueRateChanged,
                            onSearchAddress = onSearchAddress,
                            onApplySuggestedAddress = onApplySuggestedAddress,
                            onAddVenue = onAddVenue,
                            onSelectVenue = onSelectVenue
                        )
                    }

                    state.draftInvoice.sectionOrNull("client_details")
                        ?.filteredCopy(
                            ignoredKeys = setOf(
                                InvoiceFieldKeys.CLIENT_NAME,
                                InvoiceFieldKeys.CLIENT_ADDRESS
                            )
                        )
                        ?.takeIf { it.fields.isNotEmpty() }
                        ?.let { section ->
                            item {
                                InvoiceDynamicSectionCard(
                                    section = section,
                                    onFieldChanged = { fieldKey, value ->
                                        onSectionFieldChanged(section.id, fieldKey, value)
                                    }
                                )
                            }
                        }
                }

                InvoiceFormStep.WORK_DETAILS -> {
                    state.draftInvoice.sections
                        .filterNot { section ->
                            section.id in setOf("invoice_info", "worker_details", "client_details")
                        }
                        .forEach { section ->
                            item {
                                InvoiceDynamicSectionCard(
                                    section = section,
                                    onFieldChanged = { fieldKey, value ->
                                        onSectionFieldChanged(section.id, fieldKey, value)
                                    }
                                )
                            }
                        }

                    item {
                        InvoiceLineItemsCard(
                            invoice = state.draftInvoice,
                            onLineItemFieldChanged = onLineItemFieldChanged
                        )
                    }
                }

                InvoiceFormStep.REVIEW -> {
                    item {
                        InvoiceWeeklySummaryCard(
                            totalHours = state.draftInvoice.totals.totalHours,
                            hourlyRateInput = state.primaryLineRateValue()
                        )
                    }
                    item {
                        InvoiceTotalsOverviewCard(state = state)
                    }
                    itemsIndexed(state.draftInvoice.sections) { _, section ->
                        val reviewSection = section.filteredForReview()
                        if (reviewSection.fields.isNotEmpty()) {
                            InvoiceReadOnlySectionCard(section = reviewSection)
                        }
                    }
                    item {
                        InvoiceWeeklyHistorySection(
                            invoices = state.finalizedInvoices,
                            isLoading = state.isInvoiceHistoryLoading,
                            onOpenInvoice = onOpenInvoice
                        )
                    }
                }
            }

            state.error?.let { errorMessage ->
                item {
                    InvoiceNoticeCard(
                        title = stringResource(R.string.invoice_weekly_status_error),
                        body = errorMessage,
                        tone = InvoiceNoticeTone.Error
                    )
                }
            }

            state.infoMessage?.let { infoMessage ->
                item {
                    InvoiceNoticeCard(
                        title = stringResource(R.string.invoice_weekly_status_info),
                        body = infoMessage,
                        tone = InvoiceNoticeTone.Neutral
                    )
                }
            }

            state.finalizedInvoice?.let { finalized ->
                item {
                    InvoiceNoticeCard(
                        title = stringResource(R.string.invoice_weekly_finalize_action),
                        body = stringResource(
                            R.string.invoice_weekly_finalize_success,
                            finalized.invoiceNumber
                        ),
                        tone = InvoiceNoticeTone.Success
                    )
                }
            }
        }
    }
}
