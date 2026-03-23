package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun OtpTextFieldRow(
    otpDigits: List<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDigitChanged: (index: Int, value: String) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val spacing = 8.dp
        val fieldCount = otpDigits.size.coerceAtLeast(1)
        val totalSpacing = spacing * (fieldCount - 1)
        val maxFieldWidth = 52.dp
        val minFieldWidth = 36.dp
        val candidate = (maxWidth - totalSpacing) / fieldCount
        val fieldWidth = when {
            candidate < minFieldWidth -> minFieldWidth
            candidate > maxFieldWidth -> maxFieldWidth
            else -> candidate
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            otpDigits.forEachIndexed { index, digit ->
                val securityStyle = LocalAppThemePack.current.securityStyle
                val interactionSource = remember(index) { MutableInteractionSource() }
                val focused by interactionSource.collectIsFocusedAsState()
                val fieldModifier = Modifier
                    .width(fieldWidth)
                    .height(if (securityStyle.useEditorialLayout) 72.dp else 64.dp)
                    .graphicsLayer(
                        scaleX = if (focused) securityStyle.focusedInputScale else 1f,
                        scaleY = if (focused) securityStyle.focusedInputScale else 1f
                    )

                if (securityStyle.useEditorialLayout) {
                    TextField(
                        value = digit,
                        onValueChange = { value -> onDigitChanged(index, value) },
                        enabled = enabled,
                        singleLine = true,
                        interactionSource = interactionSource,
                        modifier = fieldModifier,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = MaterialTheme.colorScheme.error.copy(alpha = 0.45f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 1
                    )
                } else {
                    OutlinedTextField(
                        value = digit,
                        onValueChange = { value -> onDigitChanged(index, value) },
                        enabled = enabled,
                        singleLine = true,
                        interactionSource = interactionSource,
                        modifier = fieldModifier,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        colors = OutlinedTextFieldDefaults.colors(),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
