package net.metalbrain.paysmart.core.features.account.passkey.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun PasskeyGlowSwitch(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
        } else {
            MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.md)
        },
        label = "passkey_track"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "passkey_thumb"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) Dimens.lg else 0.dp,
        label = "passkey_thumb_offset"
    )
    val interactionSource = remember { MutableInteractionSource() }
    val trackShape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .size(width = Dimens.widthX, height = Dimens.xxl)
            .clip(trackShape)
            .background(
                brush = if (checked) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(trackColor, trackColor)
                    )
                },
                shape = trackShape
            )
            .border(
                width = 1.dp,
                color = if (checked) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = securityStyle.ghostBorderAlpha + 0.06f)
                },
                shape = trackShape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .semantics { role = Role.Switch }
            .padding(Dimens.sm)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(Dimens.lg)
                .background(color = thumbColor, shape = CircleShape)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (checked) 0.22f else 0.12f),
                    shape = CircleShape
                )
        )
    }
}
