package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import net.metalbrain.paysmart.R

@Composable
fun DontHaveAnAccount(
    modifier: Modifier = Modifier,
    onSignInClicked: () -> Unit
) {
    Row(modifier = modifier) {
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.dont_have_account))
                append(" ")
                append(stringResource(R.string.sign_up))
                append(" ")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(R.string.sign_in))
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable { onSignInClicked() }
        )
    }
}
