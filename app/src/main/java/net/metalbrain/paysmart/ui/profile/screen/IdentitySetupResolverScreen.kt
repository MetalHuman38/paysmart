package net.metalbrain.paysmart.ui.profile.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.profile.identity.provider.captureLabel
import net.metalbrain.paysmart.ui.profile.identity.component.IdentityCaptureGuide
import net.metalbrain.paysmart.ui.profile.identity.component.IdentityDocumentTypeButton
import net.metalbrain.paysmart.ui.profile.identity.provider.formattedLabel
import net.metalbrain.paysmart.ui.profile.identity.provider.frameShape
import net.metalbrain.paysmart.ui.profile.state.resolveStatus
import net.metalbrain.paysmart.ui.profile.viewmodel.IdentityResolverStep
import net.metalbrain.paysmart.ui.profile.viewmodel.IdentitySetupResolverViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IdentitySetupResolverScreen(
    viewModel: IdentitySetupResolverViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var showCameraOverlay by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes()
            } ?: throw IllegalStateException("Unable to read selected file")

            if (bytes.size > MAX_IDENTITY_UPLOAD_BYTES) {
                throw IllegalStateException("Selected file is too large")
            }

            val name = resolveDisplayName(context, uri)
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            viewModel.onDocumentCaptured(name, mimeType, bytes)
        }.onFailure {
            viewModel.onCaptureError(it.localizedMessage ?: "Unable to capture document")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.identity_resolver_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.identity_resolver_plan_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IdentityStepLine(
                        label = stringResource(R.string.identity_resolver_capture_step),
                        status = state.resolveStatus(IdentityResolverStep.CAPTURE)
                    )
                    IdentityStepLine(
                        label = stringResource(R.string.identity_resolver_encrypt_step),
                        status = state.resolveStatus(IdentityResolverStep.ENCRYPT)
                    )
                    IdentityStepLine(
                        label = stringResource(R.string.identity_resolver_upload_step),
                        status = state.resolveStatus(IdentityResolverStep.UPLOAD)
                    )
                    IdentityStepLine(
                        label = stringResource(R.string.identity_resolver_attest_step),
                        status = state.resolveStatus(IdentityResolverStep.ATTEST)
                    )
                    IdentityStepLine(
                        label = stringResource(R.string.identity_resolver_commit_step),
                        status = state.resolveStatus(IdentityResolverStep.COMMIT)
                    )
                }
            }

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
                    OutlinedButton(
                        onClick = { countryMenuExpanded = true },
                        enabled = !state.isProcessing
                    ) {
                        Text(
                            text = stringResource(
                                R.string.identity_resolver_country_selected,
                                displayCountry(state.selectedCountryIso2),
                                state.selectedCountryIso2
                            )
                        )
                    }
                    DropdownMenu(
                        expanded = countryMenuExpanded,
                        onDismissRequest = { countryMenuExpanded = false }
                    ) {
                        state.availableCountriesIso2.forEach { iso2 ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(
                                            R.string.identity_resolver_country_selected,
                                            displayCountry(iso2),
                                            iso2
                                        )
                                    )
                                },
                                onClick = {
                                    countryMenuExpanded = false
                                    viewModel.onCountryChanged(iso2)
                                }
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.identity_resolver_document_type_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.availableDocuments.forEach { document ->
                            IdentityDocumentTypeButton(
                                text = if (document.accepted) {
                                    document.formattedLabel
                                } else {
                                    stringResource(
                                        R.string.identity_resolver_document_unavailable_format,
                                        document.formattedLabel
                                    )
                                },
                                isSelected = state.selectedDocumentId == document.id,
                                onClick = { viewModel.onDocumentTypeChanged(document.id) }
                            )
                        }
                    }

                    state.selectedDocument?.let { selectedDocument ->
                        IdentityCaptureGuide(
                            selectedDocument = selectedDocument,
                            isUploadSupported = state.isSelectedDocumentUploadSupported
                        )
                    }

                    state.selectedDocumentName?.let { fileName ->
                        Text(
                            text = stringResource(
                                R.string.identity_resolver_selected_file,
                                fileName,
                                formatBytes(state.selectedDocumentSizeBytes)
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            state.error?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            state.nameMatchWarning?.takeIf { it.isNotBlank() }?.let { warning ->
                Text(
                    text = warning,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (state.isValidatingCapture) {
                Text(
                    text = stringResource(R.string.identity_resolver_capture_validating),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.currentStep == IdentityResolverStep.COMPLETE) {
                Text(
                    text = stringResource(R.string.identity_resolver_completed_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

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
                enabled = !state.isProcessing &&
                    !state.isValidatingCapture &&
                    state.selectedDocument != null,
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

            PrimaryButton(
                text = stringResource(R.string.identity_resolver_done_action),
                onClick = if (state.currentStep == IdentityResolverStep.COMPLETE) onDone else onBack,
                enabled = !state.isProcessing && !state.isValidatingCapture
            )
        }
    }

    val selectedDocument = state.selectedDocument
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







private fun resolveDisplayName(context: Context, uri: Uri): String {
    val resolver = context.contentResolver
    resolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            val value = cursor.getString(index)
            if (!value.isNullOrBlank()) {
                return value
            }
        }
    }
    return "identity_document"
}

private fun displayCountry(iso2: String): String {
    val locale = runCatching {
        Locale.Builder().setRegion(iso2).build()
    }.getOrElse {
        return iso2
    }
    return locale.getDisplayCountry(Locale.getDefault()).ifBlank { iso2 }
}

private fun formatBytes(size: Int): String {
    if (size <= 0) return "0 B"
    val kb = size / 1024.0
    return if (kb < 1024) {
        String.format(Locale.US, "%.2f KB", kb)
    } else {
        String.format(Locale.US, "%.2f MB", kb / 1024.0)
    }
}

private const val MAX_IDENTITY_UPLOAD_BYTES = 10 * 1024 * 1024
