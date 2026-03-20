package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                OutlinedTextField(
                    value = digit,
                    onValueChange = { value -> onDigitChanged(index, value) },
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier
                        .width(fieldWidth)
                        .height(64.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
