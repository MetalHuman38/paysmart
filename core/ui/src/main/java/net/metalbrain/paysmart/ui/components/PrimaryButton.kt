package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    containerColor: Color = PaysmartTheme.colorTokens.buttonPrimaryBackground,
    contentColor: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(ButtonTokens.cornerRadius),
    height: Dp = ButtonTokens.height,
    textMaxLines: Int = 1,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val colorTokens = PaysmartTheme.colorTokens
    val resolvedContentColor = if (contentColor == Color.Unspecified) {
        colorTokens.buttonPrimaryForeground
    } else {
        contentColor
    }
    val buttonStyle = LocalAppThemePack.current.buttonStyle
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
        contentPadding = ButtonTokens.contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = resolvedContentColor,
            disabledContainerColor = colorTokens.fillDisabled,
            disabledContentColor = colorTokens.textDisabled
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        )
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
