package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import net.metalbrain.paysmart.core.features.identity.component.IdentityCaptureGuide
import net.metalbrain.paysmart.core.features.identity.provider.captureLabel
import net.metalbrain.paysmart.core.features.identity.provider.formattedLabel
import net.metalbrain.paysmart.core.features.identity.provider.frameShape
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun IdentityUploadScreen(
    viewModel: IdentitySetupResolverViewModel,
    onBackToVerify: () -> Unit,
    onPendingReview: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val selectedDocument = state.selectedDocument
    val selectedCountry = remember(state.selectedCountryIso2, state.selectedCountryReviewWindow) {
        resolveIdentityCountryPresentation(context, state.selectedCountryIso2)
    }
    var showCameraOverlay by rememberSaveable { mutableStateOf(false) }
    val pickerLauncher = rememberIdentityDocumentPicker(
        onCaptured = { fileName, mimeType, bytes -> viewModel.onDocumentCaptured(fileName, mimeType, bytes) },
        onError = viewModel::onCaptureError
    )
    val openCaptureFlow = {
        viewModel.clearError()
        showCameraOverlay = true
    }

    LaunchedEffect(state.hasSubmittedForReview) {
        if (state.hasSubmittedForReview) {
            onPendingReview()
        }
    }
    val colors = PaysmartTheme.colorTokens

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
                title = stringResource(R.string.identity_resolver_title),
                subtitle = stringResource(R.string.identity_resolver_subtitle),
                onBack = onBackToVerify,
                onHelp = null
            )

            selectedDocument?.let { document ->
                IdentityUploadSummaryCard(
                    documentLabel = document.formattedLabel,
                    selectedCountry = selectedCountry,
                    selectedFileLabel = state.selectedDocumentName?.let { fileName ->
                        stringResource(
                            R.string.identity_resolver_selected_file,
                            fileName,
                            formatIdentityDocumentBytes(state.selectedDocumentSizeBytes)
                        )
                    },
                    captureGuide = {
                        IdentityCaptureGuide(
                            selectedDocument = document,
                            isUploadSupported = state.isSelectedDocumentUploadSupported
                        )
                    }
                )
            }

            IdentityResolverStatusMessages(state = state)
            IdentityVerificationPlanCard(state = state)

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                if (state.hasCapturedDocument) {
                    PrimaryButton(
                        text = stringResource(R.string.identity_resolver_submit_action),
                        onClick = viewModel::startVerification,
                        enabled = state.isSelectedDocumentAccepted &&
                            state.isSelectedDocumentUploadSupported &&
                            !state.isValidatingCapture &&
                            !state.isProcessing,
                        isLoading = state.isProcessing
                    )
                    OutlinedButton(
                        text = stringResource(R.string.identity_resolver_replace_document_action),
                        onClick = openCaptureFlow,
                        enabled = !state.isProcessing &&
                            !state.isValidatingCapture &&
                            selectedDocument != null,
                        isLoading = state.isValidatingCapture,
                        loadingText = stringResource(R.string.identity_resolver_capture_validating)
                    )
                } else {
                    PrimaryButton(
                        text = stringResource(R.string.identity_resolver_capture_document_action),
                        onClick = openCaptureFlow,
                        enabled = !state.isProcessing &&
                            !state.isValidatingCapture &&
                            selectedDocument != null,
                        isLoading = state.isValidatingCapture,
                        loadingText = stringResource(R.string.identity_resolver_capture_validating)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.xl))
        }
    }

    if (showCameraOverlay && selectedDocument != null) {
        IdentityDocumentCameraOverlay(
            frameShape = selectedDocument.frameShape,
            captureLabel = selectedDocument.captureLabel,
            onCaptured = { fileName, mimeType, bytes ->
                showCameraOverlay = false
                viewModel.onDocumentCaptured(fileName, mimeType, bytes)
            },
            onCaptureError = { message ->
                showCameraOverlay = false
                viewModel.onCaptureError(message)
            },
            onDismiss = { showCameraOverlay = false },
            onUseFileFallback = {
                showCameraOverlay = false
                pickerLauncher.launch("*/*")
            }
        )
    }
}

@Composable
private fun IdentityUploadSummaryCard(
    documentLabel: String,
    selectedCountry: IdentityCountryPresentation,
    selectedFileLabel: String?,
    captureGuide: @Composable () -> Unit
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PaysmartTheme.radius.medium,
        color = colors.surfaceElevated,
        contentColor = colors.textPrimary,
        tonalElevation = PaysmartTheme.elevation.subtle,
        border = BorderStroke(PaysmartTheme.border.thin, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                ) {
                    Text(
                        text = stringResource(R.string.identity_resolver_document_type_title),
                        style = typography.labelMedium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = documentLabel,
                        style = typography.heading4,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = PaysmartTheme.radius.pill,
                    color = colors.fillHover,
                    contentColor = colors.brandPrimary
                ) {
                    Text(
                        text = selectedCountry.reviewWindowLabel,
                        modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
                        style = typography.labelLarge,
                        color = colors.brandPrimary
                    )
                }
            }

            IdentityMetaRow(
                label = stringResource(R.string.identity_resolver_country_title),
                value = "${selectedCountry.flag} ${selectedCountry.name}"
            )
            IdentityMetaRow(
                label = stringResource(R.string.identity_resolver_review_time_title),
                value = selectedCountry.reviewWindowLabel
            )

            captureGuide()

            selectedFileLabel?.let { selectedFile ->
                Surface(
                    color = colors.surfacePrimary,
                    shape = PaysmartTheme.radius.medium
                ) {
                    Text(
                        text = selectedFile,
                        modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
                        style = typography.bodyMedium,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun IdentityMetaRow(
    label: String,
    value: String
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
        Text(
            text = label,
            style = typography.labelMedium,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = typography.bodyMedium,
            color = colors.textPrimary
        )
    }
}
