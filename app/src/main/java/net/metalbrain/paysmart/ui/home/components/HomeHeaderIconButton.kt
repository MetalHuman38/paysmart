package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
fun HomeHeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    showIndicator: Boolean = false,
    onClick: () -> Unit,
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(Dimens.minimumTouchTarget)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(Dimens.space10)
                )
            }
        }
        if (showIndicator) {
            Box(
                modifier = Modifier
                    .size(Dimens.sm)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }
    }
}
