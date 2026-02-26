package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.component.IdentityCaptureGuide
import net.metalbrain.paysmart.core.features.identity.provider.captureLabel
import net.metalbrain.paysmart.core.features.identity.provider.formattedLabel
import net.metalbrain.paysmart.core.features.identity.provider.frameShape
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityResolverStep
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun IdentityUploadScreen(
    viewModel: IdentitySetupResolverViewModel,
    onBackToVerify: () -> Unit,
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val selectedDocument = state.selectedDocument
    var showCameraOverlay by rememberSaveable { mutableStateOf(false) }
    val pickerLauncher = rememberIdentityDocumentPicker(
        onCaptured = { fileName, mimeType, bytes -> viewModel.onDocumentCaptured(fileName, mimeType, bytes) },
        onError = viewModel::onCaptureError
    )
    Scaffold(
        topBar = {
            IdentityResolverTopBar(onBack = {
                if (state.currentStep == IdentityResolverStep.COMPLETE) onDone() else onBackToVerify()
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.identity_resolver_document_type_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = selectedDocument?.formattedLabel.orEmpty(),
                style = MaterialTheme.typography.headlineSmall
            )
            selectedDocument?.let { document ->
                IdentityCaptureGuide(
                    selectedDocument = document,
                    isUploadSupported = state.isSelectedDocumentUploadSupported
                )
            }
            state.selectedDocumentName?.let { fileName ->
                Text(
                    text = stringResource(
                        R.string.identity_resolver_selected_file,
                        fileName,
                        formatIdentityDocumentBytes(state.selectedDocumentSizeBytes)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IdentityVerificationPlanCard(state = state)
            IdentityResolverStatusMessages(state = state)
            PrimaryButton(
                text = if (state.hasCapturedDocument) {
                    stringResource(R.string.identity_resolver_replace_document_action)
                } else {
                    stringResource(R.string.identity_resolver_capture_document_action)
                },
                onClick = {
                    viewModel.clearError()
                    showCameraOverlay = true
                },
                enabled = !state.isProcessing && !state.isValidatingCapture && selectedDocument != null,
                isLoading = state.isValidatingCapture,
                loadingText = stringResource(R.string.identity_resolver_capture_validating)
            )
            if (state.currentStep != IdentityResolverStep.COMPLETE) {
                PrimaryButton(
                    text = stringResource(R.string.identity_resolver_submit_action),
                    onClick = viewModel::startVerification,
                    enabled = state.hasCapturedDocument &&
                        state.isSelectedDocumentAccepted &&
                        state.isSelectedDocumentUploadSupported &&
                        !state.isValidatingCapture &&
                        !state.isProcessing,
                    isLoading = state.isProcessing
                )
            }
            OutlinedButton(
                text = if (state.currentStep == IdentityResolverStep.COMPLETE) {
                    stringResource(R.string.identity_resolver_done_action)
                } else {
                    stringResource(R.string.common_back)
                },
                onClick = {
                    if (state.currentStep == IdentityResolverStep.COMPLETE) onDone()
                    else onBackToVerify()
                },
                enabled = !state.isProcessing && !state.isValidatingCapture
            )
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
