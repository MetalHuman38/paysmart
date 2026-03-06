package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.ButtonTokens

@Composable
internal fun AuthProviderButton(
    text: String,
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = "Please wait...",
    containerColorOverride: Color? = null,
    contentColorOverride: Color? = null,
    borderColorOverride: Color? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.2f
    val defaultContainerColor = if (isDark) {
        colorScheme.surfaceVariant.copy(alpha = 0.72f)
    } else {
        colorScheme.surface
    }
    val defaultContentColor = colorScheme.onSurface
    val defaultBorderColor = if (isDark) {
        colorScheme.outline.copy(alpha = 0.9f)
    } else {
        colorScheme.outline.copy(alpha = 0.7f)
    }
    val containerColor = containerColorOverride ?: defaultContainerColor
    val contentColor = contentColorOverride ?: defaultContentColor
    val borderColor = borderColorOverride ?: defaultBorderColor

    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonTokens.height),
        shape = RoundedCornerShape(ButtonTokens.cornerRadius),
        border = BorderStroke(width = ButtonTokens.borderWidth, color = borderColor),
        contentPadding = ButtonTokens.contentPadding,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = loadingText,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge
            )
        } else {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
