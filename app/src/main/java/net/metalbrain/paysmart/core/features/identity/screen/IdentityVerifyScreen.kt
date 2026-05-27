package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

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
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Scaffold(
        containerColor = colors.backgroundPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
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
                    style = typography.labelLarge,
                    color = colors.textPrimary,
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
                    style = typography.labelLarge,
                    color = colors.textPrimary,
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
                        style = typography.bodyMedium,
                        color = colors.error
                    )
                }

                state.selectedDocument != null && !state.isSelectedDocumentUploadSupported -> {
                    Text(
                        text = stringResource(R.string.identity_resolver_document_not_supported),
                        style = typography.bodyMedium,
                        color = colors.error
                    )
                }
            }

            PrimaryButton(
                text = stringResource(R.string.continue_text),
                onClick = onNext,
                enabled = canContinue
            )

            Spacer(modifier = Modifier.height(Dimens.xl))
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
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = PaysmartTheme.radius.medium,
        color = colors.surfaceElevated,
        contentColor = colors.textPrimary,
        border = BorderStroke(
            width = PaysmartTheme.border.thin,
            color = if (enabled) {
                colors.brandPrimary.copy(alpha = 0.52f)
            } else {
                colors.borderSubtle
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
                style = typography.heading4
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.iso2,
                    style = typography.heading4,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = country.reviewWindowLabel,
                    style = typography.bodyMedium,
                    color = colors.textSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textPrimary
            )
        }
    }
}
