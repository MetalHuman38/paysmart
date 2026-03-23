package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfilePhotoActionButton

@Composable
fun ProfilePhotoActionButtons(
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProfilePhotoActionButton(
            label = stringResource(R.string.profile_change_photo_take_photo),
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
            onClick = onTakePhoto
        )
        ProfilePhotoActionButton(
            label = stringResource(R.string.profile_change_photo_from_gallery),
            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
            onClick = onPickFromGallery
        )
        ProfilePhotoActionButton(
            label = stringResource(R.string.profile_change_photo_remove),
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            onClick = onRemovePhoto
        )
    }
}
