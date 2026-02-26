package net.metalbrain.paysmart.core.features.identity.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.provider.CameraFrameShape


@Composable
fun CameraFrameShape.frameLabel(): String {
    return when (this) {
        CameraFrameShape.PASSPORT -> stringResource(R.string.identity_resolver_frame_passport)
        CameraFrameShape.CARD -> stringResource(R.string.identity_resolver_frame_card)
        CameraFrameShape.GENERIC -> stringResource(R.string.identity_resolver_frame_generic)
    }
}
