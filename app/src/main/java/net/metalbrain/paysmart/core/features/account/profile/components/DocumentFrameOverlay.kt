package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.core.features.identity.provider.CameraFrameShape


@Composable
fun DocumentFrameOverlay(
    frameShape: CameraFrameShape,
    captureLabel: String,
    modifier: Modifier = Modifier
) {
    val frameContentDescription = stringResource(
        R.string.document_frame_content_description,
        captureLabel
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.84f)
                .aspectRatio(frameAspectRatio(frameShape))
                .semantics {
                    contentDescription = frameContentDescription
                }
                .border(
                    width = 3.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = captureLabel,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun frameAspectRatio(frameShape: CameraFrameShape): Float {
    return when (frameShape) {
        CameraFrameShape.PASSPORT -> 1.42f
        CameraFrameShape.CARD -> 1.58f
        CameraFrameShape.GENERIC -> 1.30f
    }
}
