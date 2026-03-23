package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
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
import net.metalbrain.paysmart.ui.theme.HomeCardTokens

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardTokens.cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = HomeCardTokens.defaultElevation)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(Dimens.lg),
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
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = documentLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.84f)
                ) {
                    Text(
                        text = selectedCountry.reviewWindowLabel,
                        modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
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
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = selectedFile,
                        modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
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
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
