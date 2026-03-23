package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun IdentityVerifyScreen(
    viewModel: IdentitySetupResolverViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onHelp: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }

    val countryOptions = remember(state.availableCountriesIso2) {
        buildIdentityCountryPresentations(context, state.availableCountriesIso2)
    }
    val selectedCountry = remember(state.selectedCountryIso2, state.selectedCountryReviewWindow) {
        resolveIdentityCountryPresentation(context, state.selectedCountryIso2)
    }

    val canContinue = state.isSelectedDocumentAccepted &&
        state.isSelectedDocumentUploadSupported &&
        !state.isProcessing &&
        !state.isValidatingCapture

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.continue_text),
                    onClick = onNext,
                    enabled = canContinue
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            IdentityFlowHeader(
                title = stringResource(R.string.identity_selection_title),
                subtitle = stringResource(R.string.identity_selection_subtitle),
                onBack = onBack,
                onHelp = onHelp
            )

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                Text(
                    text = stringResource(R.string.identity_resolver_country_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                IdentityCountrySelectionCard(
                    country = selectedCountry,
                    enabled = !state.isProcessing && !state.isValidatingCapture,
                    onClick = { showCountrySheet = true }
                )
            }

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                Text(
                    text = stringResource(R.string.identity_resolver_document_type_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                state.availableDocuments.forEach { document ->
                    IdentityDocumentRow(
                        document = document,
                        selected = document.id == state.selectedDocumentId,
                        enabled = document.accepted,
                        onClick = { viewModel.onDocumentTypeChanged(document.id) }
                    )
                }
            }

            when {
                state.selectedDocument != null && !state.isSelectedDocumentAccepted -> {
                    Text(
                        text = stringResource(R.string.identity_resolver_document_not_accepted),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.selectedDocument != null && !state.isSelectedDocumentUploadSupported -> {
                    Text(
                        text = stringResource(R.string.identity_resolver_document_not_supported),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showCountrySheet) {
        IdentityCountrySelectionBottomSheet(
            countries = countryOptions,
            selectedCountryIso2 = state.selectedCountryIso2,
            onDismiss = { showCountrySheet = false },
            onCountrySelected = { country ->
                viewModel.onCountryChanged(country.iso2)
            }
        )
    }
}

@Composable
private fun IdentityCountrySelectionCard(
    country: IdentityCountryPresentation,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Text(
                text = country.flag,
                style = MaterialTheme.typography.headlineSmall
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.iso2,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = country.reviewWindowLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
