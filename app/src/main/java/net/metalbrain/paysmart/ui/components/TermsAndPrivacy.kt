package net.metalbrain.paysmart.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import net.metalbrain.paysmart.R

@Composable
fun TermsAndPrivacyText(
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        append(stringResource(R.string.terms_prefix).trim())
        append(" ")
        // Terms & Conditions link
        withLink(LinkAnnotation.Url("https://metalbrain.net/terms")) {
            pushStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            )

            append(stringResource(R.string.terms_and_conditions).trim())
            append(" ")
        }

        append(stringResource(R.string.terms_separator).trim()) // e.g. " and "
        append(" ")


        // Privacy Policy link
        withLink(LinkAnnotation.Url("https://metalbrain.net/privacy")) {
            pushStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            )
            append(stringResource(R.string.privacy_policy).trim())
            pop()
        }
        append(" ")

        append(stringResource(R.string.verification_policy).trim())
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier

    )
}
