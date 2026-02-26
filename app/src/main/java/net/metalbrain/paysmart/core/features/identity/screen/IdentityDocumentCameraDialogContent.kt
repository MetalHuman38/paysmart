package net.metalbrain.paysmart.core.features.identity.screen

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.DocumentFrameOverlay
import net.metalbrain.paysmart.core.features.account.profile.state.PermissionState
import net.metalbrain.paysmart.core.features.identity.provider.CameraFrameShape
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun IdentityDocumentCameraDialogContent(
    hasCameraPermission: Boolean,
    frameShape: CameraFrameShape,
    captureLabel: String,
    isCapturing: Boolean,
    onPreviewViewReady: (PreviewView) -> Unit,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    onCaptureClick: () -> Unit,
    onUseFileFallback: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        onPreviewViewReady(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            DocumentFrameOverlay(
                frameShape = frameShape,
                captureLabel = captureLabel,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PermissionState(
                onRequestPermission = onRequestPermission,
                onUseFileFallback = onUseFileFallback,
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.common_back),
                    tint = Color.White
                )
            }
        }

        if (hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.identity_resolver_camera_capture_action),
                    onClick = onCaptureClick,
                    isLoading = isCapturing,
                    loadingText = stringResource(R.string.identity_resolver_camera_capturing)
                )
                OutlinedButton(
                    onClick = onUseFileFallback,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.identity_resolver_capture_fallback_action))
                }
            }
        }
    }
}
