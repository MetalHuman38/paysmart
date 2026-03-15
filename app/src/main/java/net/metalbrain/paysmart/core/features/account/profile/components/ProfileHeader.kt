package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens

private val ProfileHeaderAvatarSize = 116.dp
private val ProfileHeaderAvatarOverlap = 58.dp
private val ProfileHeaderAvatarTop = ProfileHeaderBannerHeight - ProfileHeaderAvatarOverlap
private val ProfileHeaderStackHeight = ProfileHeaderAvatarTop + ProfileHeaderAvatarSize

@Composable
fun ProfileHeader(
    displayName: String,
    contactText: String?,
    photoURL: String?,
    isVerified: Boolean,
    onChangePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedName = displayName.ifBlank { stringResource(R.string.profile_default_name) }
    val supportingText = contactText?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.profile_change_photo_contact_fallback)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ProfileHeaderStackHeight),
            contentAlignment = Alignment.TopCenter
        ) {
            ProfileHeaderBanner(modifier = Modifier.fillMaxWidth())

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = ProfileHeaderAvatarTop)
            ) {
                ProfileAvatarImage(
                    displayName = resolvedName,
                    photoModel = photoURL,
                    size = ProfileHeaderAvatarSize
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 2.dp, bottom = 2.dp)
                ) {
                    IconButton(
                        onClick = onChangePhotoClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = stringResource(R.string.profile_change_photo_action)
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = Dimens.space6, bottom = Dimens.space6)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = resolvedName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = if (isVerified) {
                        stringResource(R.string.profile_verified_status_content_description)
                    } else {
                        stringResource(R.string.profile_pending_status_content_description)
                    },
                    tint = if (isVerified) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .padding(start = Dimens.sm)
                        .size(20.dp)
                )
            }

            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.sm)
            )
        }
    }
}
