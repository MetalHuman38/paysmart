package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileAvatarImage
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileAvatarPreset

@Composable
fun ProfilePhotoPresetGrid(
    displayName: String,
    presets: List<ProfileAvatarPreset>,
    selectedToken: String?,
    onSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        presets.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelected(preset.token) },
                        contentAlignment = Alignment.TopEnd
                    ) {
                        ProfileAvatarImage(
                            displayName = displayName,
                            photoModel = preset.token,
                            size = 92.dp
                        )
                        if (preset.token == selectedToken) {
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                repeat((3 - row.size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
