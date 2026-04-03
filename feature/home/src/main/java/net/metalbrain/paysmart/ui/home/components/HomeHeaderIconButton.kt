package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme


@Composable
fun HomeHeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    badgeCount: Int = 0,
    showIndicator: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = PaysmartTheme.colorTokens
    Box(contentAlignment = Alignment.TopEnd) {
        Surface(
            shape = CircleShape,
            color = colors.surfaceElevated.copy(alpha = 0.94f)
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(Dimens.minimumTouchTarget)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = colors.brandPrimary,
                    modifier = Modifier.size(Dimens.space10)
                )
            }
        }
        if (showIndicator || badgeCount > 0) {
            if (badgeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = colors.brandPrimary,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textInverse
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(Dimens.sm)
                        .background(
                            color = colors.brandPrimary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
