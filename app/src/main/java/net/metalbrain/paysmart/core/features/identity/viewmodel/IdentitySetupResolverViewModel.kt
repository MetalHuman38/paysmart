package net.metalbrain.paysmart.core.features.identity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.profile.data.type.KycDocumentType
import net.metalbrain.paysmart.core.features.identity.data.ClientInformation
import net.metalbrain.paysmart.core.features.identity.provider.IdentityDocumentTextExtraction
import net.metalbrain.paysmart.core.features.identity.provider.IdentityDocumentTextExtractor
import net.metalbrain.paysmart.core.features.identity.provider.IdentityImageAuthenticityDetector
import net.metalbrain.paysmart.core.features.identity.provider.IdentityImageDecision
import net.metalbrain.paysmart.core.features.identity.provider.KycDocumentCatalog
import net.metalbrain.paysmart.core.features.identity.state.IdentitySetupResolverUiState
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityDocumentType
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityUploadExtractionSnapshot
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityUploadOrchestrator
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityUploadPipelineStage
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import java.util.Locale

enum class IdentityResolverStep {
    CAPTURE,
    ENCRYPT,
    UPLOAD,
    ATTEST,
    COMMIT,
    COMPLETE
}



@HiltViewModel
class IdentitySetupResolverViewModel @Inject constructor(
    private val uploadOrchestrator: IdentityUploadOrchestrator,
    private val authRepository: AuthRepository,
    private val userProfileCacheRepository: UserProfileCacheRepository,
    private val imageAuthenticityDetector: IdentityImageAuthenticityDetector,
    private val textExtractor: IdentityDocumentTextExtractor
) : ViewModel() {

    private var capturedBytes: ByteArray? = null
    private var capturedExtraction: IdentityDocumentTextExtraction? = null

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<IdentitySetupResolverUiState> = _uiState.asStateFlow()

    init {
        hydrateCountryFromCachedProfile()
    }

    fun onCountryChanged(iso2: String) {
        val country = KycDocumentCatalog.resolveCountry(iso2)
        val documents = KycDocumentCatalog.documentsForCountry(country)
        val selectedId = defaultDocumentId(documents)

        capturedBytes = null
        capturedExtraction = null
        _uiState.update {
            it.copy(
                selectedCountryIso2 = country,
                availableDocuments = documents,
                selectedDocumentId = selectedId,
                selectedDocumentName = null,
                selectedMimeType = null,
                selectedDocumentSizeBytes = 0,
                hasCapturedDocument = false,
                currentStep = IdentityResolverStep.CAPTURE,
                isValidatingCapture = false,
                failedStep = null,
                receipt = null,
                nameMatchWarning = null,
                error = null
            )
        }
    }

    fun onDocumentTypeChanged(documentId: String) {
        val shouldResetCapture = _uiState.value.selectedDocumentId != documentId
        if (shouldResetCapture) {
            capturedBytes = null
            capturedExtraction = null
        }
        _uiState.update { current ->
            if (current.availableDocuments.none { it.id == documentId }) {
                return@update current
            }
            current.copy(
                selectedDocumentId = documentId,
                selectedDocumentName = if (shouldResetCapture) null else current.selectedDocumentName,
                selectedMimeType = if (shouldResetCapture) null else current.selectedMimeType,
                selectedDocumentSizeBytes = if (shouldResetCapture) 0 else current.selectedDocumentSizeBytes,
                hasCapturedDocument = if (shouldResetCapture) false else current.hasCapturedDocument,
                currentStep = if (shouldResetCapture) IdentityResolverStep.CAPTURE else current.currentStep,
                isValidatingCapture = false,
                nameMatchWarning = if (shouldResetCapture) null else current.nameMatchWarning,
                error = null
            )
        }
    }

    fun onDocumentCaptured(
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isValidatingCapture = true,
                    failedStep = null,
                    error = null
                )
            }

            val detectionResult = imageAuthenticityDetector.detect(bytes, mimeType).getOrElse { error ->
                capturedBytes = null
                capturedExtraction = null
                _uiState.update {
                    it.copy(
                        isValidatingCapture = false,
                        selectedDocumentName = null,
                        selectedMimeType = null,
                        selectedDocumentSizeBytes = 0,
                        hasCapturedDocument = false,
                        currentStep = IdentityResolverStep.CAPTURE,
                        failedStep = IdentityResolverStep.CAPTURE,
                        nameMatchWarning = null,
                        error = error.localizedMessage ?: "Unable to validate captured image"
                    )
                }
                return@launch
            }

            if (detectionResult.decision == IdentityImageDecision.SUSPECTED_SYNTHETIC) {
                capturedBytes = null
                capturedExtraction = null
                _uiState.update {
                    it.copy(
                        isValidatingCapture = false,
                        selectedDocumentName = null,
                        selectedMimeType = null,
                        selectedDocumentSizeBytes = 0,
                        hasCapturedDocument = false,
                        currentStep = IdentityResolverStep.CAPTURE,
                        failedStep = IdentityResolverStep.CAPTURE,
                        nameMatchWarning = null,
                        error = "Captured image appears synthetic. Retake using the physical document."
                    )
                }
                return@launch
            }

            val extraction = textExtractor.extract(bytes, mimeType).getOrNull()
            val nameMatchWarning = resolveNameMatchWarning(extraction?.candidateFullName)

            capturedBytes = bytes
            capturedExtraction = extraction
            _uiState.update {
                it.copy(
                    selectedDocumentName = fileName,
                    selectedMimeType = mimeType,
                    selectedDocumentSizeBytes = bytes.size,
                    hasCapturedDocument = true,
                    currentStep = IdentityResolverStep.CAPTURE,
                    isValidatingCapture = false,
                    failedStep = null,
                    receipt = null,
                    nameMatchWarning = nameMatchWarning,
                    error = null
                )
            }
        }
    }

    fun onCaptureError(message: String) {
        _uiState.update {
            it.copy(
                isValidatingCapture = false,
                error = message,
                nameMatchWarning = null,
                failedStep = IdentityResolverStep.CAPTURE
            )
        }
    }

    fun startVerification() {
        val bytes = capturedBytes
        if (bytes == null || bytes.isEmpty()) {
            _uiState.update {
                it.copy(
                    error = "Capture a document before submitting",
                    failedStep = IdentityResolverStep.CAPTURE
                )
            }
            return
        }

        if (_uiState.value.isProcessing || _uiState.value.isValidatingCapture) {
            return
        }

        viewModelScope.launch {
            val state = _uiState.value
            val selectedDocument = state.selectedDocument
            if (selectedDocument == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        isValidatingCapture = false,
                        error = "Select a document type before submitting",
                        failedStep = IdentityResolverStep.CAPTURE
                    )
                }
                return@launch
            }
            if (!selectedDocument.accepted) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        isValidatingCapture = false,
                        error = "Selected document type is currently not accepted",
                        failedStep = IdentityResolverStep.CAPTURE
                    )
                }
                return@launch
            }
            val uploadDocumentType = selectedDocument.toUploadDocumentType()
            if (uploadDocumentType == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        isValidatingCapture = false,
                        error = "Selected document type is not yet supported for upload",
                        failedStep = IdentityResolverStep.CAPTURE
                    )
                }
                return@launch
            }
            val clientInformation = resolveClientInformation(state.selectedCountryIso2)
            if (clientInformation == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        isValidatingCapture = false,
                        error = "Complete legal name, email, and date of birth before identity upload",
                        failedStep = IdentityResolverStep.CAPTURE
                    )
                }
                return@launch
            }

            _uiState.update {
                    it.copy(
                        isProcessing = true,
                        isValidatingCapture = false,
                        failedStep = null,
                        receipt = null,
                        error = null,
                    currentStep = IdentityResolverStep.ENCRYPT
                )
            }

            val result = uploadOrchestrator.uploadDocument(
                documentType = uploadDocumentType,
                documentBytes = bytes,
                contentType = state.selectedMimeType ?: "image/jpeg",
                clientInformation = clientInformation,
                extraction = capturedExtraction.toUploadExtractionSnapshot(),
                onStageChanged = { stage ->
                    _uiState.update { current ->
                        current.copy(
                            currentStep = stage.toResolverStep(),
                            failedStep = null
                        )
                    }
                }
            )

            result
                .onSuccess { receipt ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            isValidatingCapture = false,
                            currentStep = IdentityResolverStep.COMPLETE,
                            failedStep = null,
                            receipt = receipt,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    val failed = _uiState.value.currentStep
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            isValidatingCapture = false,
                            failedStep = failed,
                            error = error.localizedMessage ?: "Identity verification upload failed"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, nameMatchWarning = null) }
    }

    private fun hydrateCountryFromCachedProfile() {
        viewModelScope.launch {
            val uid = authRepository.currentUser?.uid ?: return@launch
            val cachedProfile = userProfileCacheRepository.observeByUid(uid).firstOrNull() ?: return@launch
            val preferredIso2 = resolveCountryIso2FromCachedValue(cachedProfile.country) ?: return@launch
            if (_uiState.value.selectedCountryIso2 != preferredIso2) {
                onCountryChanged(preferredIso2)
            }
        }
    }

    private suspend fun resolveNameMatchWarning(extractedCandidate: String?): String? {
        val expectedName = resolveExpectedProfileName() ?: return null
        val extractedName = extractedCandidate
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        if (isLikelyNameMatch(expectedName, extractedName)) {
            return null
        }

        return "Document name differs from profile name. You can continue, but this may require manual review."
    }

    private suspend fun resolveExpectedProfileName(): String? {
        val uid = authRepository.currentUser?.uid ?: return null
        val cached = userProfileCacheRepository.observeByUid(uid).firstOrNull()
        val fromCache = cached?.displayName?.trim().orEmpty()
        if (fromCache.isNotEmpty()) return fromCache

        val fromAuth = authRepository.currentUser?.displayName?.trim().orEmpty()
        return fromAuth.ifEmpty { null }
    }

    private suspend fun resolveClientInformation(selectedCountryIso2: String): ClientInformation? {
        val uid = authRepository.currentUser?.uid ?: return null
        val cached = userProfileCacheRepository.observeByUid(uid).firstOrNull()
        val legalName = cached?.displayName?.trim().orEmpty()
            .ifBlank { authRepository.currentUser?.displayName?.trim().orEmpty() }
        val email = cached?.email?.trim().orEmpty()
            .ifBlank { authRepository.currentUser?.email?.trim().orEmpty() }
        val dateOfBirth = cached?.dateOfBirth?.trim().orEmpty()
        if (legalName.isBlank() || email.isBlank() || dateOfBirth.isBlank()) {
            return null
        }

        val nameParts = splitLegalName(legalName)
        if (nameParts.firstName.isBlank() || nameParts.lastName.isBlank()) {
            return null
        }

        val countryIso2 = resolveCountryIso2FromCachedValue(cached?.country) ?: selectedCountryIso2
        return ClientInformation(
            firstName = nameParts.firstName,
            middleName = nameParts.middleName,
            lastName = nameParts.lastName,
            email = email,
            dateOfBirth = dateOfBirth,
            countryIso2 = countryIso2
        )
    }
}

private fun createInitialState(): IdentitySetupResolverUiState {
    val initialCountry = KycDocumentCatalog.resolveCountry(Locale.getDefault().country)
    val documents = KycDocumentCatalog.documentsForCountry(initialCountry)
    return IdentitySetupResolverUiState(
        selectedCountryIso2 = initialCountry,
        availableCountriesIso2 = KycDocumentCatalog.supportedCountriesIso2,
        availableDocuments = documents,
        selectedDocumentId = defaultDocumentId(documents)
    )
}

private fun defaultDocumentId(documents: List<KycDocumentType>): String {
    return documents.firstOrNull { it.accepted }?.id
        ?: documents.firstOrNull()?.id
        ?: "passport"
}

private fun KycDocumentType.toUploadDocumentType(): IdentityDocumentType? {
    return when (id.lowercase(Locale.US)) {
        "passport" -> IdentityDocumentType.PASSPORT
        "drivers_license" -> IdentityDocumentType.DRIVERS_LICENSE
        "national_id" -> IdentityDocumentType.NATIONAL_ID
        else -> null
    }
}

private fun resolveCountryIso2FromCachedValue(rawCountryValue: String?): String? {
    val raw = rawCountryValue?.trim().orEmpty()
    if (raw.isBlank()) return null

    if (raw.length == 2) {
        return KycDocumentCatalog.resolveCountry(raw)
    }

    return KycDocumentCatalog.supportedCountriesIso2.firstOrNull { iso2 ->
        iso2.equals(raw, ignoreCase = true) ||
            displayCountryName(iso2, Locale.getDefault()).equals(raw, ignoreCase = true) ||
            displayCountryName(iso2, Locale.ENGLISH).equals(raw, ignoreCase = true)
    }
}

private fun displayCountryName(iso2: String, locale: Locale): String {
    return runCatching {
        Locale.Builder()
            .setRegion(iso2)
            .build()
            .getDisplayCountry(locale)
    }.getOrDefault("")
}

private fun isLikelyNameMatch(expected: String, extracted: String): Boolean {
    val expectedTokens = tokenizeName(expected)
    val extractedTokens = tokenizeName(extracted)
    if (expectedTokens.isEmpty() || extractedTokens.isEmpty()) return true

    val overlap = expectedTokens.count { token -> extractedTokens.contains(token) }
    val threshold = minOf(2, expectedTokens.size)
    return overlap >= threshold || overlap.toFloat() / expectedTokens.size >= 0.6f
}

private fun tokenizeName(value: String): Set<String> {
    return value
        .trim()
        .lowercase(Locale.US)
        .replace(Regex("[^a-z0-9 ]"), " ")
        .split(Regex("\\s+"))
        .filter { token -> token.length >= 2 }
        .toSet()
}

private data class LegalNameParts(
    val firstName: String,
    val middleName: String?,
    val lastName: String
)

private fun splitLegalName(fullName: String): LegalNameParts {
    val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> LegalNameParts("", null, "")
        parts.size == 1 -> LegalNameParts(parts.first(), null, "")
        parts.size == 2 -> LegalNameParts(parts.first(), null, parts.last())
        else -> LegalNameParts(
            firstName = parts.first(),
            middleName = parts.subList(1, parts.lastIndex).joinToString(" ").ifBlank { null },
            lastName = parts.last()
        )
    }
}

private fun IdentityDocumentTextExtraction?.toUploadExtractionSnapshot():
    IdentityUploadExtractionSnapshot? {
    this ?: return null
    return IdentityUploadExtractionSnapshot(
        candidateFullName = candidateFullName?.trim()?.ifBlank { null },
        provider = provider
    )
}

private fun IdentityUploadPipelineStage.toResolverStep(): IdentityResolverStep {
    return when (this) {
        IdentityUploadPipelineStage.ENCRYPT -> IdentityResolverStep.ENCRYPT
        IdentityUploadPipelineStage.UPLOAD -> IdentityResolverStep.UPLOAD
        IdentityUploadPipelineStage.ATTEST -> IdentityResolverStep.ATTEST
        IdentityUploadPipelineStage.COMMIT -> IdentityResolverStep.COMMIT
    }
}
