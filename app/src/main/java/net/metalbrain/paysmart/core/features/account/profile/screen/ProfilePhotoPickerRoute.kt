package net.metalbrain.paysmart.core.features.account.profile.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileAvatarCatalog
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.ProfilePhotoUiState
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.ProfilePhotoViewModel
import net.metalbrain.paysmart.domain.model.AuthUserModel


private class PendingPhotoUpload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val previewModel: Any
)

@Composable
fun ProfilePhotoPickerRoute(
    user: AuthUserModel,
    uiState: ProfilePhotoUiState,
    viewModel: ProfilePhotoViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionError = stringResource(
        R.string.profile_change_photo_camera_permission_error
    )
    var selectedPresetToken by remember {
        mutableStateOf(ProfileAvatarCatalog.presetForPhotoUrl(user.photoURL)?.token)
    }
    var removePhotoSelected by remember { mutableStateOf(false) }
    var pendingUpload by remember { mutableStateOf<PendingPhotoUpload?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var pendingCameraLaunch by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.completedAt) {
        if (uiState.completedAt != null) {
            onBack()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching { prepareProfilePhoto(context, uri) }
            .onSuccess { payload ->
                pendingUpload = PendingPhotoUpload(
                    fileName = payload.fileName,
                    mimeType = payload.mimeType,
                    bytes = payload.bytes,
                    previewModel = payload.previewModel
                )
                selectedPresetToken = null
                removePhotoSelected = false
                viewModel.clearError()
            }
            .onFailure { error ->
                viewModel.setError(error.localizedMessage ?: "Unable to update photo right now.")
            }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val captured = cameraUri
        if (!success || captured == null) return@rememberLauncherForActivityResult
        runCatching { prepareProfilePhoto(context, captured) }
            .onSuccess { payload ->
                pendingUpload = PendingPhotoUpload(
                    fileName = payload.fileName,
                    mimeType = payload.mimeType,
                    bytes = payload.bytes,
                    previewModel = payload.previewModel
                )
                selectedPresetToken = null
                removePhotoSelected = false
                viewModel.clearError()
            }
            .onFailure { error ->
                viewModel.setError(error.localizedMessage ?: "Unable to update photo right now.")
            }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted && pendingCameraLaunch) {
            pendingCameraLaunch = false
            val nextUri = createProfilePhotoCaptureUri(context)
            cameraUri = nextUri
            runCatching { cameraLauncher.launch(nextUri) }
                .onFailure {
                    cameraUri = null
                    viewModel.setError(cameraPermissionError)
                }
        } else if (!granted) {
            pendingCameraLaunch = false
            viewModel.setError(cameraPermissionError)
        }
    }

    val selectedPhotoModel: Any? = when {
        removePhotoSelected -> null
        pendingUpload != null -> pendingUpload?.previewModel
        selectedPresetToken != null -> selectedPresetToken
        else -> user.photoURL
    }
    val hasChanges = pendingUpload != null ||
        removePhotoSelected != user.photoURL.isNullOrBlank() ||
        selectedPresetToken != ProfileAvatarCatalog.presetForPhotoUrl(user.photoURL)?.token

    ProfilePhotoPickerScreen(
        displayName = user.displayName.orEmpty(),
        selectedPhotoModel = selectedPhotoModel,
        selectedPresetToken = selectedPresetToken,
        isSaving = uiState.isSaving,
        hasChanges = hasChanges,
        errorMessage = uiState.errorMessage,
        onBack = onBack,
        onPresetSelected = { token ->
            selectedPresetToken = token
            pendingUpload = null
            removePhotoSelected = false
            viewModel.clearError()
        },
        onTakePhoto = {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            hasCameraPermission = granted
            if (granted) {
                val nextUri = createProfilePhotoCaptureUri(context)
                cameraUri = nextUri
                runCatching { cameraLauncher.launch(nextUri) }
                    .onFailure {
                        cameraUri = null
                        viewModel.setError(cameraPermissionError)
                    }
            } else {
                pendingCameraLaunch = true
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onPickFromGallery = { galleryLauncher.launch("image/*") },
        onRemovePhoto = {
            selectedPresetToken = null
            pendingUpload = null
            removePhotoSelected = true
            viewModel.clearError()
        },
        onSave = {
            when {
                pendingUpload != null -> {
                    pendingUpload?.let { payload ->
                        viewModel.uploadProfilePhoto(
                            fileName = payload.fileName,
                            mimeType = payload.mimeType,
                            bytes = payload.bytes
                        )
                    } ?: onBack()
                }

                removePhotoSelected -> viewModel.removeProfilePhoto()

                !selectedPresetToken.isNullOrBlank() -> {
                    viewModel.savePresetAvatar(selectedPresetToken.orEmpty())
                }

                else -> onBack()
            }
        }
    )
}

