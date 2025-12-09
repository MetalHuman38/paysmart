package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
    onDigitChanged: (index: Int, value: String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        otpDigits.forEachIndexed { index, digit ->
            OutlinedTextField(
                value = digit,
                onValueChange = { value -> onDigitChanged(index, value) },
                singleLine = true,
                modifier = Modifier
                    .width(50.dp)
                    .height(64.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                maxLines = 1
            )
        }
    }
}
