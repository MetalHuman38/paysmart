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


@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = Color.Unspecified,
    textMaxLines: Int = 1,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val resolvedContentColor = if (contentColor == Color.Unspecified) {
        contentColorFor(containerColor)
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
