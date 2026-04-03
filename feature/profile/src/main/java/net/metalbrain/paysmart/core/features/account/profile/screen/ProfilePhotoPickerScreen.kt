package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileAvatarCatalog
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileAvatarImage
import net.metalbrain.paysmart.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoPickerScreen(
    displayName: String,
    selectedPhotoModel: Any?,
    selectedPresetToken: String?,
    isSaving: Boolean,
    hasChanges: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onPresetSelected: (String) -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_change_photo_title)) },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ProfileAvatarImage(
                displayName = displayName,
                photoModel = selectedPhotoModel,
                size = 132.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = stringResource(R.string.profile_change_photo_presets_title),
                style = MaterialTheme.typography.titleMedium
            )
            ProfilePhotoPresetGrid(
                displayName = displayName,
                presets = ProfileAvatarCatalog.presets,
                selectedToken = selectedPresetToken,
                onSelected = onPresetSelected
            )

            Text(
                text = stringResource(R.string.profile_change_photo_upload_title),
                style = MaterialTheme.typography.titleMedium
            )
            ProfilePhotoActionButtons(
                onTakePhoto = onTakePhoto,
                onPickFromGallery = onPickFromGallery,
                onRemovePhoto = onRemovePhoto
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = if (isSaving) {
                    stringResource(R.string.profile_change_photo_saving)
                } else {
                    stringResource(R.string.profile_change_photo_save)
                },
                onClick = onSave,
                enabled = hasChanges && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )
        }
    }
}
