package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.ButtonTokens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    containerColor: Color = PaysmartTheme.colorTokens.surfacePrimary,
    contentColor: Color = Color.Unspecified,
    borderColor: Color = PaysmartTheme.colorTokens.borderStrong,
    borderWidth: Dp = ButtonTokens.borderWidth,
    contentPadding: PaddingValues = ButtonTokens.contentPadding,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    shape: Shape = RoundedCornerShape(ButtonTokens.cornerRadius),
    height: Dp = ButtonTokens.height,
    textMaxLines: Int = 1,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
    ) {
    val colorTokens = PaysmartTheme.colorTokens
    val resolvedContentColor = if (contentColor == Color.Unspecified) {
        colorTokens.textPrimary
    } else {
        contentColor
    }
    val buttonStyle = LocalAppThemePack.current.buttonStyle
    val resolvedShape = if (buttonStyle.useFullPillButtons) {
        RoundedCornerShape(percent = 50)
    } else {
        shape
    }
    val resolvedContainerColor = if (buttonStyle.useFullPillButtons) {
        Color.Transparent
    } else {
        containerColor
    }
    val resolvedBorderColor = if (buttonStyle.useFullPillButtons) {
        colorTokens.borderSubtle.copy(alpha = buttonStyle.ghostBorderAlpha)
    } else {
        borderColor
    }

    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = resolvedShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = resolvedContainerColor,
            contentColor = resolvedContentColor,
            disabledContainerColor = colorTokens.fillDisabled,
            disabledContentColor = colorTokens.textDisabled
        ),
        border = BorderStroke(borderWidth, resolvedBorderColor),
        elevation = if (buttonStyle.useFullPillButtons) {
            ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
                disabledElevation = 0.dp
            )
        } else {
            elevation
        },
        contentPadding = contentPadding

    ) {
        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = resolvedContentColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = loadingText ?: text,
                    color = resolvedContentColor,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
        } else {
            Text(
                text = text,
                color = resolvedContentColor,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = textMaxLines,
                overflow = textOverflow,
                softWrap = textMaxLines > 1
            )
        }
    }
}
