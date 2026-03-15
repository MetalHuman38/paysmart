package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

val ProfileHeaderBannerHeight = 176.dp

@Composable
fun ProfileHeaderBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ProfileHeaderBannerHeight)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.52f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .offset(x = 20.dp, y = 22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.22f))
        )
        Box(
            modifier = Modifier
                .size(168.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-14).dp, y = (-18).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.14f))
        )
        Box(
            modifier = Modifier
                .size(76.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-28).dp, y = (-18).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.18f))
        )
    }
}
