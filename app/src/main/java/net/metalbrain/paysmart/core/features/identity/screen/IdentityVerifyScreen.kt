package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.provider.formattedLabel
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.ui.components.CatalogSelectionBottomSheet
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun IdentityVerifyScreen(
    viewModel: IdentitySetupResolverViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }
    var showDocumentSheet by rememberSaveable { mutableStateOf(false) }

    val countryOptions = remember(state.availableCountriesIso2) {
        buildIdentityCountryOptions(context, state.availableCountriesIso2)
    }
    val selectedCountry = remember(state.selectedCountryIso2, state.availableCountriesIso2) {
        resolveIdentityCountryPresentation(context, state.selectedCountryIso2)
    }
    val canContinue = state.selectedDocument?.accepted == true

    Scaffold(topBar = { IdentityResolverTopBar(onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.identity_resolver_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.identity_resolver_country_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    IdentityResolverSelectionField(
                        text = stringResource(
                            R.string.identity_resolver_country_selected,
                            selectedCountry.name,
                            selectedCountry.iso2
                        ),
                        enabled = !state.isProcessing && !state.isValidatingCapture,
                        onClick = { showCountrySheet = true }
                    )

                    Text(
                        text = stringResource(R.string.identity_resolver_document_type_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    IdentityResolverSelectionField(
                        text = state.selectedDocument?.formattedLabel
                            ?: stringResource(R.string.identity_resolver_document_type_title),
                        enabled = !state.isProcessing && !state.isValidatingCapture,
                        onClick = { showDocumentSheet = true }
                    )
                }
            }

            if (!canContinue && state.selectedDocument != null) {
                Text(
                    text = stringResource(R.string.identity_resolver_document_not_accepted),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            PrimaryButton(
                text = stringResource(R.string.continue_text),
                contentColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onNext,
                enabled = canContinue && !state.isProcessing && !state.isValidatingCapture
            )
        }
    }

    if (showCountrySheet) {
        CatalogSelectionBottomSheet(
            title = stringResource(R.string.identity_resolver_country_title),
            options = countryOptions,
            selectedKey = state.selectedCountryIso2,
            onDismiss = { showCountrySheet = false },
            onSelect = { option -> viewModel.onCountryChanged(option.key) }
        )
    }

    if (showDocumentSheet) {
        IdentityDocumentSelectionBottomSheet(
            documents = state.availableDocuments,
            selectedDocumentId = state.selectedDocumentId,
            onDismiss = { showDocumentSheet = false },
            onDocumentSelected = { document -> viewModel.onDocumentTypeChanged(document.id) }
        )
    }
}
