package net.metalbrain.paysmart.core.features.invoicing.routing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSetupFlowScreen
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupViewModel

@Composable
fun InvoiceSetupRoute(
    viewModel: InvoiceSetupViewModel,
    onBack: () -> Unit,
    onOpenInvoice: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    InvoiceSetupFlowScreen(
        state = state,
        onExit = onBack,
        onBackStep = viewModel::goToPreviousFormStep,
        onSelectProfession = viewModel::selectProfession,
        onSelectTemplate = viewModel::applyTemplate,
        onSectionFieldChanged = viewModel::updateSectionField,
        onVenueNameChanged = viewModel::updateVenueNameInput,
        onVenueAddressChanged = viewModel::updateVenueAddressInput,
        onVenueCountryChanged = viewModel::updateVenueCountryInput,
        onVenueRateChanged = viewModel::updateVenueRateInput,
        onSearchAddress = viewModel::searchVenueAddress,
        onApplySuggestedAddress = viewModel::applySuggestedVenueAddress,
        onAddVenue = viewModel::addVenue,
        onSelectVenue = viewModel::selectVenue,
        onLineItemFieldChanged = viewModel::updateLineItemField,
        onSaveDraft = viewModel::saveDraft,
        onContinue = viewModel::goToNextFormStep,
        onFinalize = viewModel::finalizeInvoice,
        onOpenInvoice = onOpenInvoice
    )
}
