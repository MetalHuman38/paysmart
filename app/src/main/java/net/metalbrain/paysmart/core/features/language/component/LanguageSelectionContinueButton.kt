package net.metalbrain.paysmart.core.features.language.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun LanguageSelectionContinueButton(
    onClick: () -> Unit
) {
    PrimaryButton(
        text = stringResource(R.string.continue_text),
        onClick = onClick
    )
}
