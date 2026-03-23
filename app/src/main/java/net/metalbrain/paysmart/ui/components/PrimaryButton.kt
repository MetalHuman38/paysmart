package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.ButtonTokens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(ButtonTokens.cornerRadius),
    height: Dp = ButtonTokens.height,
    textMaxLines: Int = 1,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val resolvedContentColor = if (contentColor == Color.Unspecified) {
        contentColorFor(containerColor)
    } else {
        contentColor
    }
    val themePack = LocalAppThemePack.current
    val buttonStyle = themePack.buttonStyle
    val resolvedShape = if (buttonStyle.useFullPillButtons) {
        RoundedCornerShape(percent = 50)
    } else {
        shape
    }
    val isButtonEnabled = enabled && !isLoading

    Button(
        onClick = onClick,
        enabled = isButtonEnabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = resolvedShape,
        contentPadding = if (buttonStyle.usePrimaryGradient) {
            PaddingValues(0.dp)
        } else {
            ButtonTokens.contentPadding
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (buttonStyle.usePrimaryGradient) {
                Color.Transparent
            } else {
                containerColor
            },
            contentColor = resolvedContentColor,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (buttonStyle.usePrimaryGradient) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(
                        brush = if (isButtonEnabled) {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                            )
                        },
                        shape = resolvedShape
                    )
                    .drawWithContent {
                        drawContent()
                        val glowHeight = 1.5.dp.toPx()
                        drawRect(
                            color = Color.White.copy(alpha = buttonStyle.primaryGlowAlpha),
                            size = size.copy(height = glowHeight)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                PrimaryButtonLabel(
                    text = text,
                    isLoading = isLoading,
                    loadingText = loadingText,
                    resolvedContentColor = resolvedContentColor,
                    textMaxLines = textMaxLines,
                    textOverflow = textOverflow
                )
            }
        } else {
            PrimaryButtonLabel(
                text = text,
                isLoading = isLoading,
                loadingText = loadingText,
                resolvedContentColor = resolvedContentColor,
                textMaxLines = textMaxLines,
                textOverflow = textOverflow
            )
        }
    }
}

@Composable
private fun PrimaryButtonLabel(
    text: String,
    isLoading: Boolean,
    loadingText: String?,
    resolvedContentColor: Color,
    textMaxLines: Int,
    textOverflow: TextOverflow,
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
            softWrap = textMaxLines > 1,
        )
    }
}
