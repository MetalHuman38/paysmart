package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun InvoiceVenueSetupRoute(
    viewModel: InvoiceSetupViewModel,
    onBack: () -> Unit,
    onRequireProfile: () -> Unit,
    onContinue: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.isHydrating, state.profileDraft.isValid) {
        if (!state.isHydrating && !state.profileDraft.isValid) {
            onRequireProfile()
        }
    }
    InvoiceVenueSetupScreen(
        state = state,
        onBack = onBack,
        onVenueNameChanged = viewModel::updateVenueNameInput,
        onVenueAddressChanged = viewModel::updateVenueAddressInput,
        onVenueCountryChanged = viewModel::updateVenueCountryInput,
        onVenueRateChanged = viewModel::updateVenueRateInput,
        onSearchAddress = viewModel::searchVenueAddress,
        onApplySuggestedAddress = viewModel::applySuggestedVenueAddress,
        onAddVenue = viewModel::addVenue,
        onSelectVenue = viewModel::selectVenue,
        onContinue = { viewModel.proceedToWeekly(onContinue) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceVenueSetupScreen(
    state: InvoiceSetupUiState,
    onBack: () -> Unit,
    onVenueNameChanged: (String) -> Unit,
    onVenueAddressChanged: (String) -> Unit,
    onVenueCountryChanged: (String) -> Unit,
    onVenueRateChanged: (String) -> Unit,
    onSearchAddress: () -> Unit,
    onApplySuggestedAddress: () -> Unit,
    onAddVenue: () -> Unit,
    onSelectVenue: (String) -> Unit,
    onContinue: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.material3.Text(stringResource(R.string.invoice_venue_setup_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isHydrating) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            item {
                InvoiceGuideCard(
                    title = stringResource(R.string.invoice_venue_setup_intro_title),
                    body = stringResource(R.string.invoice_venue_setup_intro_body)
                )
            }
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
            item {
                PrimaryButton(
                    text = stringResource(R.string.invoice_venue_setup_continue_action),
                    onClick = onContinue,
                    enabled = state.canProceedToWeekly && !state.isPersisting,
                    isLoading = state.isPersisting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
