package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.ui.R
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun EmailInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                if (placeholder.isBlank()) {
                    stringResource(R.string.email_placeholder)
                } else {
                    placeholder
                }
            )
        },
        isError = isError,
        singleLine = true,
        supportingText = {
            if (isError) {
                Text(
                    text = stringResource(R.string.email_invalid),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(text = stringResource(R.string.email_enter_prompt))
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.xs),
        shape = MaterialTheme.shapes.medium
    )
}
