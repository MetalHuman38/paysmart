package net.metalbrain.paysmart.core.features.identity.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import net.metalbrain.paysmart.core.features.identity.provider.CameraFrameShape

@Composable
fun IdentityDocumentCameraOverlay(
    frameShape: CameraFrameShape,
    captureLabel: String,
    onCaptured: (fileName: String, mimeType: String, bytes: ByteArray) -> Unit,
    onCaptureError: (String) -> Unit,
    onDismiss: () -> Unit,
    onUseFileFallback: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    LaunchedEffect(hasCameraPermission, previewView) {
        if (!hasCameraPermission || previewView == null) return@LaunchedEffect
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                runCatching {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder()
                        .build()
                        .also { useCase -> useCase.surfaceProvider = previewView?.surfaceProvider }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        capture
                    )
                    imageCapture = capture
                }.onFailure { error ->
                    onCaptureError(error.localizedMessage ?: "Unable to start camera")
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
        }
    }
    val onCaptureClick: () -> Unit = captureClick@{
        val capture = imageCapture
        if (capture == null || isCapturing) return@captureClick
        isCapturing = true
        captureIdentityDocument(
            imageCapture = capture,
            cacheDir = context.cacheDir,
            executor = ContextCompat.getMainExecutor(context),
            onSuccess = { name, mimeType, bytes ->
                isCapturing = false
                onCaptured(name, mimeType, bytes)
            },
            onFailure = { message ->
                isCapturing = false
                onCaptureError(message)
            }
        )
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        IdentityDocumentCameraDialogContent(
            hasCameraPermission = hasCameraPermission,
            frameShape = frameShape,
            captureLabel = captureLabel,
            isCapturing = isCapturing,
            onPreviewViewReady = { previewView = it },
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onDismiss = onDismiss,
            onCaptureClick = onCaptureClick,
            onUseFileFallback = onUseFileFallback
        )
    }
}
