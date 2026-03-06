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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun InvoiceWorkerProfileRoute(
    viewModel: InvoiceSetupViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    InvoiceWorkerProfileScreen(
        state = state,
        onBack = onBack,
        onFullNameChanged = viewModel::updateProfileFullName,
        onAddressChanged = viewModel::updateProfileAddress,
        onBadgeNumberChanged = viewModel::updateProfileBadgeNumber,
        onBadgeExpiryChanged = viewModel::updateProfileBadgeExpiryDate,
        onUtrChanged = viewModel::updateProfileUtrNumber,
        onEmailChanged = viewModel::updateProfileEmail,
        onPhoneChanged = viewModel::updateProfileContactPhone,
        onAccountNumberChanged = viewModel::updateProfileAccountNumber,
        onSortCodeChanged = viewModel::updateProfileSortCode,
        onPaymentInstructionsChanged = viewModel::updateProfilePaymentInstructions,
        onDefaultRateChanged = viewModel::updateProfileDefaultHourlyRate,
        onContinue = { viewModel.proceedToVenue(onContinue) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceWorkerProfileScreen(
    state: InvoiceSetupUiState,
    onBack: () -> Unit,
    onFullNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onBadgeNumberChanged: (String) -> Unit,
    onBadgeExpiryChanged: (String) -> Unit,
    onUtrChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onAccountNumberChanged: (String) -> Unit,
    onSortCodeChanged: (String) -> Unit,
    onPaymentInstructionsChanged: (String) -> Unit,
    onDefaultRateChanged: (String) -> Unit,
    onContinue: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invoice_profile_setup_title)) },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text(text = stringResource(R.string.invoice_profile_setup_subtitle)) }
            item {
                InvoiceProfileSetupSection(
                    state = state,
                    onFullNameChanged = onFullNameChanged,
                    onAddressChanged = onAddressChanged,
                    onBadgeNumberChanged = onBadgeNumberChanged,
                    onBadgeExpiryChanged = onBadgeExpiryChanged,
                    onUtrChanged = onUtrChanged,
                    onEmailChanged = onEmailChanged,
                    onPhoneChanged = onPhoneChanged,
                    onAccountNumberChanged = onAccountNumberChanged,
                    onSortCodeChanged = onSortCodeChanged,
                    onPaymentInstructionsChanged = onPaymentInstructionsChanged,
                    onDefaultRateChanged = onDefaultRateChanged
                )
            }
            state.error?.let { item { Text(text = it) } }
            state.infoMessage?.let { item { Text(text = it) } }
            item {
                PrimaryButton(
                    text = stringResource(R.string.invoice_profile_setup_continue_action),
                    onClick = onContinue,
                    enabled = state.canProceedToVenue && !state.isPersisting,
                    isLoading = state.isPersisting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
