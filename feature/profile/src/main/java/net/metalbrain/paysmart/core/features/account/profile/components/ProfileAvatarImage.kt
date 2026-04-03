package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileAvatarImage(
    displayName: String,
    photoModel: Any?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val resolvedName = displayName.ifBlank { "PaySmart User" }
    val preset = ProfileAvatarCatalog.presetForPhotoUrl(photoModel as? String)
    when {
        preset != null -> {
            Image(
                painter = painterResource(id = preset.drawableResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
            )
        }

        photoModel != null -> {
            val context = LocalContext.current
            val avatarSizePx = with(androidx.compose.ui.platform.LocalDensity.current) {
                size.roundToPx().coerceAtLeast(1)
            }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoModel)
                    .size(avatarSizePx)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
            )
        }

        else -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsFromName(resolvedName),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

fun initialsFromName(displayName: String): String {
    val parts = displayName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
    if (parts.isEmpty()) {
        return "PS"
    }
    return when {
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts[1].first()}".uppercase()
    }
}
