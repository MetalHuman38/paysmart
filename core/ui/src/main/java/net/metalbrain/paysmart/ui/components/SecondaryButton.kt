package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.ButtonTokens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack
import net.metalbrain.paysmart.ui.theme.PaysmartTheme


@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = PaysmartTheme.colorTokens.buttonSecondaryBackground,
    contentColor: Color = Color.Unspecified,
    textMaxLines: Int = 1,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val colorTokens = PaysmartTheme.colorTokens
    val resolvedContentColor = if (contentColor == Color.Unspecified) {
        colorTokens.textPrimary
    } else {
        contentColor
    }
    val shape = if (LocalAppThemePack.current.buttonStyle.useFullPillButtons) {
        RoundedCornerShape(percent = 50)
    } else {
        RoundedCornerShape(ButtonTokens.cornerRadius)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonTokens.height),
        shape = shape,
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
        Text(
            text = text,
            color = resolvedContentColor,
            style = MaterialTheme.typography.labelLarge,
            maxLines = textMaxLines,
            overflow = textOverflow,
            textAlign = TextAlign.Center,
            softWrap = textMaxLines > 1
        )
    }
}
