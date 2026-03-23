package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun PasscodeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    showText: Boolean,
    onToggleVisibility: () -> Unit,
    isError: Boolean = false
) {
    val themePack = LocalAppThemePack.current
    val securityStyle = themePack.securityStyle
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val modifier = Modifier
        .fillMaxWidth()
        .graphicsLayer(
            scaleX = if (focused) securityStyle.focusedInputScale else 1f,
            scaleY = if (focused) securityStyle.focusedInputScale else 1f
        )

    if (securityStyle.useEditorialLayout) {
        TextField(
            value = value,
            onValueChange = { updated ->
                onValueChange(
                    updated.filter(Char::isDigit).take(6)
                )
            },
            label = { Text(label) },
            singleLine = true,
            isError = isError,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(22.dp),
            textStyle = MaterialTheme.typography.titleLarge,
            visualTransformation = if (showText) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (showText) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showText) {
                            stringResource(R.string.passcode_hide)
                        } else {
                            stringResource(R.string.passcode_show)
                        }
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                errorIndicatorColor = MaterialTheme.colorScheme.error.copy(alpha = 0.45f),
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = modifier
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = { updated ->
                onValueChange(
                    updated.filter(Char::isDigit).take(6)
                )
            },
            label = { Text(label) },
            singleLine = true,
            isError = isError,
            interactionSource = interactionSource,
            shape = MaterialTheme.shapes.medium,
            visualTransformation = if (showText) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (showText) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showText) {
                            stringResource(R.string.passcode_hide)
                        } else {
                            stringResource(R.string.passcode_show)
                        }
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = modifier
        )
    }
}
